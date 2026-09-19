package problems.p06_movieticketbooking;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One seat with its own lock — the fine-grained unit. Every mutator below is
 * called ONLY while holding lock(); multi-seat operations acquire seat locks
 * in ID order (2PL discipline) so they can never deadlock.
 */
public class Seat {
    private final String id;
    private final ReentrantLock lock = new ReentrantLock();

    private SeatStatus status = SeatStatus.AVAILABLE;
    private String holdId;
    private Instant holdExpiry;

    Seat(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    ReentrantLock lock() {
        return lock;
    }

    public SeatStatus status() {
        return status;
    }

    /** Free, or held by a hold that has lapsed (lazy expiry — no timer per seat). */
    boolean canHold(Instant now) {
        return status == SeatStatus.AVAILABLE
                || (status == SeatStatus.HELD && holdExpiry.isBefore(now));
    }

    String currentHoldId() {
        return holdId;
    }

    boolean isHeldBy(String expectedHoldId) {
        return status == SeatStatus.HELD && expectedHoldId.equals(holdId);
    }

    void hold(String newHoldId, Instant expiry) {
        status = SeatStatus.HELD;
        holdId = newHoldId;
        holdExpiry = expiry;
    }

    void book() {
        status = SeatStatus.BOOKED;
        holdId = null;
        holdExpiry = null;
    }

    void release() {
        status = SeatStatus.AVAILABLE;
        holdId = null;
        holdExpiry = null;
    }
}
