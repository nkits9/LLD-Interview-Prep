package problems.extras.cabbooking;

public class Trip {
    private final String id;
    private final String rider;
    private final String driverId;
    private final Location pickup;
    private final Location drop;
    private final int surgeBasisPoints; // locked in at REQUEST time — riders hate re-quotes
    private TripStatus status = TripStatus.REQUESTED;
    private long farePaise = -1;

    Trip(String id, String rider, String driverId, Location pickup, Location drop, int surgeBasisPoints) {
        this.id = id;
        this.rider = rider;
        this.driverId = driverId;
        this.pickup = pickup;
        this.drop = drop;
        this.surgeBasisPoints = surgeBasisPoints;
    }

    public String id() {
        return id;
    }

    public String driverId() {
        return driverId;
    }

    Location drop() {
        return drop;
    }

    public int surgeBasisPoints() {
        return surgeBasisPoints;
    }

    public synchronized TripStatus status() {
        return status;
    }

    synchronized void transitionTo(TripStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("illegal transition " + status + " -> " + next);
        }
        status = next;
    }

    public synchronized long farePaise() {
        return farePaise;
    }

    synchronized void setFare(long farePaise) {
        this.farePaise = farePaise;
    }

    @Override
    public String toString() {
        return id + " " + rider + " driver=" + driverId + " " + pickup + "->" + drop
                + " surge=" + surgeBasisPoints / 100 + "% " + status();
    }
}
