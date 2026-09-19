package problems.extras.irctc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One train on one date — the unit of contention. The FAIR lock serializes
 * bookings on THIS run only (other trains/dates never contend) and serves
 * waiting bookers FIFO: the "handle concurrent requests in a fair manner"
 * requirement, verbatim.
 */
public class TrainRun {
    private final List<Seat> seats;
    private final ReentrantLock lock = new ReentrantLock(true); // fair = FIFO, no barging

    TrainRun(int seatCount) {
        List<Seat> created = new ArrayList<>();
        for (int number = 1; number <= seatCount; number++) {
            created.add(new Seat(number));
        }
        this.seats = List.copyOf(created);
    }

    /**
     * All-or-nothing: choose {@code count} seats free across the whole mask and
     * occupy them, atomically. Allocation is a cross-seat check-then-act, which
     * is exactly why the lock is per-run, not per-seat.
     */
    public List<Seat> book(long segmentMask, int count, SeatAllocationStrategy strategy) {
        lock.lock();
        try {
            List<Seat> chosen = strategy.allocate(seats, segmentMask, count);
            if (chosen.size() < count) {
                throw new NoSeatsAvailableException(
                        "only " + chosen.size() + " of " + count + " requested seats free on those segments");
            }
            for (Seat seat : chosen) {
                seat.occupy(segmentMask);
            }
            return chosen;
        } finally {
            lock.unlock();
        }
    }

    public void release(List<Integer> seatNumbers, long segmentMask) {
        lock.lock();
        try {
            for (Seat seat : seats) {
                if (seatNumbers.contains(seat.number())) {
                    seat.release(segmentMask);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /** Same lock as booking — a plain long can tear/stale-read if written concurrently. */
    public int availableCount(long segmentMask) {
        lock.lock();
        try {
            int free = 0;
            for (Seat seat : seats) {
                if (seat.isFreeFor(segmentMask)) {
                    free++;
                }
            }
            return free;
        } finally {
            lock.unlock();
        }
    }
}
