package problems.p11_fooddelivery;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class Order {
    private final String id;
    private final String customer;
    private final String restaurantId;
    private final int customerLocation;
    private final long totalPaise;
    private final long deliveryFeePaise;
    private final Instant placedAt;

    private OrderStatus status = OrderStatus.PLACED;
    // CAS on assignment: two riders accepting simultaneously — exactly one wins.
    private final AtomicReference<String> riderId = new AtomicReference<>();

    Order(String id, String customer, String restaurantId, int customerLocation,
          long totalPaise, long deliveryFeePaise, Instant placedAt) {
        this.id = id;
        this.customer = customer;
        this.restaurantId = restaurantId;
        this.customerLocation = customerLocation;
        this.totalPaise = totalPaise;
        this.deliveryFeePaise = deliveryFeePaise;
        this.placedAt = placedAt;
    }

    public String id() {
        return id;
    }

    public String customer() {
        return customer;
    }

    public String restaurantId() {
        return restaurantId;
    }

    int customerLocation() {
        return customerLocation;
    }

    public long totalPaise() {
        return totalPaise;
    }

    public long deliveryFeePaise() {
        return deliveryFeePaise;
    }

    Instant placedAt() {
        return placedAt;
    }

    public String rider() {
        return riderId.get();
    }

    public synchronized OrderStatus status() {
        return status;
    }

    /** Atomic validated transition; returns the previous status (for notify/fee rules). */
    synchronized OrderStatus transitionTo(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalTransitionException(status, next);
        }
        OrderStatus previous = status;
        status = next;
        return previous;
    }

    /** Cancellation is transition + rule in ONE atomic step — no stale-status fee decisions. */
    synchronized OrderStatus cancelReturningPrevious() {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new CancellationNotAllowedException(status);
        }
        OrderStatus previous = status;
        status = OrderStatus.CANCELLED;
        return previous;
    }

    boolean assignRider(String rider) {
        return riderId.compareAndSet(null, rider);
    }

    boolean unassignRider(String rider) {
        return riderId.compareAndSet(rider, null);
    }
}
