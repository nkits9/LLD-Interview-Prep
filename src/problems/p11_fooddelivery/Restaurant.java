package problems.p11_fooddelivery;

import java.util.concurrent.Semaphore;

/** Kitchen capacity = a Semaphore: accept acquires a slot, pickup/cancel releases it. */
public class Restaurant {
    private final String id;
    private final int location;
    private final Semaphore kitchenSlots;

    public Restaurant(String id, int location, int maxConcurrentOrders) {
        this.id = id;
        this.location = location;
        this.kitchenSlots = new Semaphore(maxConcurrentOrders);
    }

    public String id() {
        return id;
    }

    public int location() {
        return location;
    }

    boolean tryTakeKitchenSlot() {
        return kitchenSlots.tryAcquire(); // fail fast — never block a user-facing call
    }

    void releaseKitchenSlot() {
        kitchenSlots.release();
    }

    public int freeSlots() {
        return kitchenSlots.availablePermits();
    }
}
