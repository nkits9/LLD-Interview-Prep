package problems.extras.irctc;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Orchestrates search-availability-book-cancel. Holds no locks itself —
 * mutual exclusion lives inside each TrainRun, so trains and dates never
 * contend with each other.
 */
public class BookingService {
    private final TrainCatalog catalog;
    private final SeatAllocationStrategy allocation;
    private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicLong ticketSeq = new AtomicLong(); // ids generated in one place

    public BookingService(TrainCatalog catalog, SeatAllocationStrategy allocation) {
        this.catalog = catalog;
        this.allocation = allocation;
    }

    /** Seats free across EVERY segment of from→to on that date. */
    public int availableSeats(String trainId, LocalDate date, String from, String to) {
        Train train = catalog.byNumber(trainId);
        return train.runOn(date).availableCount(train.segmentMask(from, to));
    }

    /** All-or-nothing for {@code seatCount} seats; confirmed-only, no waitlist. */
    public Ticket book(String user, String trainId, LocalDate date, String from, String to, int seatCount) {
        if (seatCount < 1) {
            throw new IllegalArgumentException("seatCount must be >= 1, got " + seatCount);
        }
        Train train = catalog.byNumber(trainId);
        long mask = train.segmentMask(from, to); // validates route before touching inventory
        var seats = train.runOn(date).book(mask, seatCount, allocation);

        Ticket ticket = new Ticket("TKT-" + ticketSeq.incrementAndGet(), user, trainId, date, from, to,
                seats.stream().map(Seat::number).collect(Collectors.toList()));
        tickets.put(ticket.id(), ticket);
        return ticket;
    }

    /** Frees exactly the ticket's segments — the seat may already be re-sold on OTHER segments. */
    public void cancel(String ticketId) {
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new TicketNotFoundException(ticketId);
        }
        ticket.markCancelled(); // atomic status flip: double-cancel throws here
        Train train = catalog.byNumber(ticket.trainId());
        train.runOn(ticket.date())
                .release(ticket.seatNumbers(), train.segmentMask(ticket.from(), ticket.to()));
    }
}
