package problems.p03_parkinglot;

import java.time.Instant;

/** The bridge between entry and exit — immutable. */
public final class Ticket {
    private final String id;
    private final Vehicle vehicle;
    private final String spotId;
    private final Instant entryTime;

    Ticket(String id, Vehicle vehicle, String spotId, Instant entryTime) {
        this.id = id;
        this.vehicle = vehicle;
        this.spotId = spotId;
        this.entryTime = entryTime;
    }

    public String id() {
        return id;
    }

    public Vehicle vehicle() {
        return vehicle;
    }

    public String spotId() {
        return spotId;
    }

    public Instant entryTime() {
        return entryTime;
    }

    @Override
    public String toString() {
        return id + " " + vehicle + " @ " + spotId;
    }
}
