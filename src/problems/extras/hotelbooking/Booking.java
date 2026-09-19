package problems.extras.hotelbooking;

import java.time.LocalDate;

public final class Booking {
    public enum Status { CONFIRMED, CANCELLED }

    private final String id;
    private final String guest;
    private final RoomType type;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int rooms;
    private Status status = Status.CONFIRMED;

    Booking(String id, String guest, RoomType type, LocalDate checkIn, LocalDate checkOut, int rooms) {
        this.id = id;
        this.guest = guest;
        this.type = type;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.rooms = rooms;
    }

    public String id() {
        return id;
    }

    RoomType type() {
        return type;
    }

    LocalDate checkIn() {
        return checkIn;
    }

    LocalDate checkOut() {
        return checkOut;
    }

    int rooms() {
        return rooms;
    }

    synchronized void markCancelled() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("booking " + id + " is already cancelled");
        }
        status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        return id + " " + guest + " " + rooms + "x" + type + " " + checkIn + "->" + checkOut;
    }
}
