package problems.p06_movieticketbooking;

import java.time.Instant;
import java.util.List;

/**
 * Reserve-with-TTL. CONFIRMING guards the payment window so a hold can't be
 * confirmed twice or expired mid-payment; transitions happen under the hold's
 * monitor — payment I/O never does.
 */
public class Hold {
    public enum Status { ACTIVE, CONFIRMING, CONFIRMED, RELEASED, EXPIRED }

    private final String id;
    private final String showId;
    private final String user;
    private final List<String> seatIds;
    private final Instant expiresAt;
    private Status status = Status.ACTIVE;

    Hold(String id, String showId, String user, List<String> seatIds, Instant expiresAt) {
        this.id = id;
        this.showId = showId;
        this.user = user;
        this.seatIds = List.copyOf(seatIds);
        this.expiresAt = expiresAt;
    }

    public String id() {
        return id;
    }

    String showId() {
        return showId;
    }

    String user() {
        return user;
    }

    public List<String> seatIds() {
        return seatIds;
    }

    Instant expiresAt() {
        return expiresAt;
    }

    public synchronized Status status() {
        return status;
    }

    synchronized void transitionTo(Status next) {
        this.status = next;
    }

    /** Atomic claim of the confirmation window; false = someone else got there first. */
    synchronized boolean tryStartConfirming(Instant now) {
        if (status != Status.ACTIVE || expiresAt.isBefore(now)) {
            return false;
        }
        status = Status.CONFIRMING;
        return true;
    }
}
