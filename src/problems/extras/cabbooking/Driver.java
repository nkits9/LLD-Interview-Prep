package problems.extras.cabbooking;

import java.util.concurrent.atomic.AtomicBoolean;

/** Availability is a CAS flag — two concurrent requests can never claim one driver. */
public class Driver {
    private final String id;
    private volatile Location location;
    private final AtomicBoolean available = new AtomicBoolean(true);

    public Driver(String id, Location location) {
        this.id = id;
        this.location = location;
    }

    public String id() {
        return id;
    }

    public Location location() {
        return location;
    }

    void moveTo(Location newLocation) {
        this.location = newLocation;
    }

    public boolean isAvailable() {
        return available.get();
    }

    boolean tryClaim() {
        return available.compareAndSet(true, false);
    }

    void release() {
        available.set(true);
    }
}
