package problems.p06_movieticketbooking;

import java.util.List;

public final class Booking {
    public enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final String showId;
    private final String user;
    private final List<String> seatIds;
    private final long amountPaise;
    private Status status = Status.CONFIRMED;

    Booking(String id, String showId, String user, List<String> seatIds, long amountPaise) {
        this.id = id;
        this.showId = showId;
        this.user = user;
        this.seatIds = List.copyOf(seatIds);
        this.amountPaise = amountPaise;
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

    long amountPaise() {
        return amountPaise;
    }

    public synchronized Status status() {
        return status;
    }

    synchronized void markCancelled() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("booking " + id + " is already cancelled");
        }
        status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        return id + " " + status() + " " + user + " seats" + seatIds;
    }
}
