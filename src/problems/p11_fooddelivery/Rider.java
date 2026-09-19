package problems.p11_fooddelivery;

import java.util.concurrent.atomic.AtomicBoolean;

/** Availability is a CAS flag — claiming a rider and assigning an order are both races, both atomic. */
public class Rider {
    private final String id;
    private final int location;
    private final AtomicBoolean available = new AtomicBoolean(true);

    public Rider(String id, int location) {
        this.id = id;
        this.location = location;
    }

    public String id() {
        return id;
    }

    public int location() {
        return location;
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
