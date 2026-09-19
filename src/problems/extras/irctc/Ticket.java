package problems.extras.irctc;

import java.time.LocalDate;
import java.util.List;

/** Confirmed-only (no waitlist). Carries everything cancellation needs to free exactly its segments. */
public final class Ticket {
    public enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final String user;
    private final String trainId;
    private final LocalDate date;
    private final String from;
    private final String to;
    private final List<Integer> seatNumbers;
    private Status status = Status.CONFIRMED;

    Ticket(String id, String user, String trainId, LocalDate date,
           String from, String to, List<Integer> seatNumbers) {
        this.id = id;
        this.user = user;
        this.trainId = trainId;
        this.date = date;
        this.from = from;
        this.to = to;
        this.seatNumbers = List.copyOf(seatNumbers);
    }

    /** synchronized: two concurrent cancels must not both pass the status check. */
    synchronized void markCancelled() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("ticket " + id + " is already cancelled");
        }
        status = Status.CANCELLED;
    }

    public String id() {
        return id;
    }

    public String trainId() {
        return trainId;
    }

    public LocalDate date() {
        return date;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public List<Integer> seatNumbers() {
        return seatNumbers;
    }

    public synchronized Status status() {
        return status;
    }

    @Override
    public String toString() {
        return id + " " + status() + " " + user + " " + trainId + " " + date
                + " " + from + "->" + to + " seats" + seatNumbers;
    }
}
