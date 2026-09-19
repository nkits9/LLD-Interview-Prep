package problems.p03_parkinglot;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Fine-grained concurrency unit: occupancy is an AtomicReference, so parallel
 * entry gates contend per-spot via CAS — the lot is never globally locked.
 */
public class Spot {
    private final String id;
    private final SpotType type;
    private final AtomicReference<Vehicle> occupant = new AtomicReference<>();

    public Spot(String id, SpotType type) {
        this.id = id;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public SpotType type() {
        return type;
    }

    public boolean isFree() {
        return occupant.get() == null;
    }

    public Vehicle occupant() {
        return occupant.get();
    }

    /** The check IS the write — no check-then-act window. */
    boolean tryPark(Vehicle vehicle) {
        return type.canFit(vehicle.type()) && occupant.compareAndSet(null, vehicle);
    }

    boolean free(Vehicle expected) {
        return occupant.compareAndSet(expected, null);
    }
}
