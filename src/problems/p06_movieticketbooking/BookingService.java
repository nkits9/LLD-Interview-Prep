package problems.p06_movieticketbooking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * The concurrency centrepiece:
 *  - hold: acquire ALL seat locks in ID order (2PL), validate all, mutate all,
 *    release — all-or-nothing.
 *  - confirm: claim the hold (CONFIRMING), charge payment with NO locks held,
 *    then book on success or release on failure (saga compensation).
 *  - idempotent: same idempotency key → same booking, never a second charge.
 */
public class BookingService {
    private final Map<String, Show> shows = new ConcurrentHashMap<>();
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final Map<String, Booking> byIdempotencyKey = new ConcurrentHashMap<>();
    private final List<BookingObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();

    private final PaymentGateway gateway;
    private final Clock clock;
    private final Duration holdTtl;
    private final long pricePerSeatPaise;

    public BookingService(PaymentGateway gateway, Clock clock, Duration holdTtl, long pricePerSeatPaise) {
        this.gateway = gateway;
        this.clock = clock;
        this.holdTtl = holdTtl;
        this.pricePerSeatPaise = pricePerSeatPaise;
    }

    public void addShow(Show show) {
        shows.put(show.id(), show);
    }

    public void subscribe(BookingObserver observer) {
        observers.add(observer);
    }

    /** All-or-nothing hold with TTL. Seat locks acquired in ID order — 2PL, no deadlock. */
    public Hold hold(String showId, List<String> seatIds, String user) {
        Show show = showOrThrow(showId);
        List<Seat> seats = seatIds.stream().sorted()                  // global lock order
                .map(show::seat).collect(Collectors.toList());
        seats.forEach(seat -> seat.lock().lock());                    // growing phase
        try {
            Instant now = clock.instant();
            List<String> blocked = seats.stream()
                    .filter(seat -> !seat.canHold(now))
                    .map(Seat::id).collect(Collectors.toList());
            if (!blocked.isEmpty()) {
                throw new SeatsUnavailableException(blocked);         // nothing was mutated
            }
            for (Seat seat : seats) {                                 // lazy-expire lapsed holds
                if (seat.status() == SeatStatus.HELD) {
                    Hold lapsed = holds.get(seat.currentHoldId());
                    if (lapsed != null) {
                        lapsed.transitionTo(Hold.Status.EXPIRED);
                    }
                }
            }
            Hold hold = new Hold("HOLD-" + seq.incrementAndGet(), showId, user, seatIds,
                    now.plus(holdTtl));
            seats.forEach(seat -> seat.hold(hold.id(), hold.expiresAt()));
            holds.put(hold.id(), hold);
            return hold;
        } finally {
            seats.forEach(seat -> seat.lock().unlock());              // shrinking phase
        }
    }

    /**
     * Idempotent confirm. Payment retries / double-clicks with the same key
     * return the SAME booking and charge exactly once.
     */
    public Booking confirm(String holdId, String idempotencyKey) {
        Booking existing = byIdempotencyKey.get(idempotencyKey);
        if (existing != null) {
            return existing;                                          // replay: no second charge
        }
        Hold hold = holds.get(holdId);
        if (hold == null) {
            throw new HoldInvalidException("unknown hold " + holdId);
        }
        if (!hold.tryStartConfirming(clock.instant())) {              // atomic claim of the window
            throw new HoldInvalidException("hold " + holdId + " is " + hold.status()
                    + (hold.status() == Hold.Status.ACTIVE ? " but expired" : ""));
        }

        long amount = hold.seatIds().size() * pricePerSeatPaise;
        boolean paid = gateway.charge(hold.user(), amount, idempotencyKey); // NO locks held: I/O

        if (!paid) {                                                  // saga: compensate
            releaseSeats(hold);
            hold.transitionTo(Hold.Status.RELEASED);
            throw new PaymentFailedException(holdId);
        }
        Show show = showOrThrow(hold.showId());
        List<Seat> seats = hold.seatIds().stream().sorted().map(show::seat).collect(Collectors.toList());
        seats.forEach(seat -> seat.lock().lock());
        try {
            seats.forEach(Seat::book);
        } finally {
            seats.forEach(seat -> seat.lock().unlock());
        }
        hold.transitionTo(Hold.Status.CONFIRMED);
        Booking booking = new Booking("BKG-" + seq.incrementAndGet(), hold.showId(),
                hold.user(), hold.seatIds(), amount);
        bookings.put(booking.id(), booking);
        byIdempotencyKey.put(idempotencyKey, booking);
        observers.forEach(o -> o.onConfirmed(booking));               // outside all locks
        return booking;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("no booking " + bookingId);
        }
        booking.markCancelled();                                      // throws on double cancel
        Show show = showOrThrow(booking.showId());
        for (String seatId : booking.seatIds().stream().sorted().collect(Collectors.toList())) {
            Seat seat = show.seat(seatId);
            seat.lock().lock();
            try {
                seat.release();
            } finally {
                seat.lock().unlock();
            }
        }
        gateway.refund(booking.user(), booking.amountPaise());
    }

    /** Sweep for expired ACTIVE holds (prod: scheduled); lazy expiry also runs in hold(). */
    public int expireHolds() {
        int expired = 0;
        Instant now = clock.instant();
        for (Hold hold : holds.values()) {
            synchronized (hold) {
                if (hold.status() != Hold.Status.ACTIVE || !hold.expiresAt().isBefore(now)) {
                    continue;
                }
                hold.transitionTo(Hold.Status.EXPIRED);
            }
            releaseSeats(hold);
            expired++;
        }
        return expired;
    }

    private void releaseSeats(Hold hold) {
        Show show = showOrThrow(hold.showId());
        for (String seatId : hold.seatIds().stream().sorted().collect(Collectors.toList())) {
            Seat seat = show.seat(seatId);
            seat.lock().lock();
            try {
                if (seat.isHeldBy(hold.id())) {                        // never clobber a newer hold
                    seat.release();
                }
            } finally {
                seat.lock().unlock();
            }
        }
    }

    private Show showOrThrow(String showId) {
        Show show = shows.get(showId);
        if (show == null) {
            throw new IllegalArgumentException("no show " + showId);
        }
        return show;
    }
}
