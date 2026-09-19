package problems.p09_inventory;

import java.util.Map;

public final class Reservation {
    public enum Status { ACTIVE, COMMITTED, RELEASED }

    private final String id;
    private final String sku;
    private final Map<String, Integer> allocations; // warehouseId -> qty
    private Status status = Status.ACTIVE;

    Reservation(String id, String sku, Map<String, Integer> allocations) {
        this.id = id;
        this.sku = sku;
        this.allocations = Map.copyOf(allocations);
    }

    public String id() {
        return id;
    }

    String sku() {
        return sku;
    }

    public Map<String, Integer> allocations() {
        return allocations;
    }

    public synchronized Status status() {
        return status;
    }

    /** Atomic guard: a reservation is consumed exactly once. */
    synchronized void transition(Status next) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("reservation " + id + " is already " + status);
        }
        status = next;
    }
}
