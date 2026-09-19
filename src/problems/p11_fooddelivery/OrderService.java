package problems.p11_fooddelivery;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Event-driven lifecycle orchestration; every transition is validated and observed. */
public class OrderService {
    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
    private final Map<String, Rider> riders = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final List<OrderObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();

    private final RiderMatchingStrategy matching;
    private final DeliveryFeeStrategy feeStrategy;
    private final Clock clock;
    private final Duration acceptTimeout;
    private final long lateCancellationFeePaise;

    public OrderService(RiderMatchingStrategy matching, DeliveryFeeStrategy feeStrategy,
                        Clock clock, Duration acceptTimeout, long lateCancellationFeePaise) {
        this.matching = matching;
        this.feeStrategy = feeStrategy;
        this.clock = clock;
        this.acceptTimeout = acceptTimeout;
        this.lateCancellationFeePaise = lateCancellationFeePaise;
    }

    public void register(Restaurant restaurant) {
        restaurants.put(restaurant.id(), restaurant);
    }

    public void register(Rider rider) {
        riders.put(rider.id(), rider);
    }

    public void subscribe(OrderObserver observer) {
        observers.add(observer);
    }

    public Order place(String customer, String restaurantId, int customerLocation, long totalPaise) {
        Restaurant restaurant = restaurant(restaurantId);
        long fee = feeStrategy.feePaise(Math.abs(restaurant.location() - customerLocation));
        Order order = new Order("ORD-" + seq.incrementAndGet(), customer, restaurantId,
                customerLocation, totalPaise, fee, clock.instant());
        orders.put(order.id(), order);
        notifyAll(order, null, OrderStatus.PLACED);
        return order;
    }

    /** Kitchen slot (semaphore) acquired at accept; released at pickup or cancel. */
    public void accept(String orderId) {
        Order order = order(orderId);
        Restaurant restaurant = restaurant(order.restaurantId());
        if (!restaurant.tryTakeKitchenSlot()) {
            throw new RestaurantAtCapacityException(restaurant.id());
        }
        try {
            transition(order, OrderStatus.ACCEPTED);
        } catch (IllegalStateException e) {
            restaurant.releaseKitchenSlot(); // failed transition must not leak the slot
            throw e;
        }
    }

    public void startPreparing(String orderId) {
        transition(order(orderId), OrderStatus.PREPARING);
    }

    public void pickUp(String orderId) {
        Order order = order(orderId);
        if (order.rider() == null) {
            throw new IllegalStateException("no rider assigned to " + orderId);
        }
        transition(order, OrderStatus.PICKED_UP);
        restaurant(order.restaurantId()).releaseKitchenSlot();
    }

    public void deliver(String orderId) {
        Order order = order(orderId);
        transition(order, OrderStatus.DELIVERED);
        Rider rider = riders.get(order.rider());
        if (rider != null) {
            rider.release();
        }
    }

    /** A rider accepts: claim the rider AND win the order CAS — or cleanly back out. */
    public boolean riderAccepts(String orderId, String riderId) {
        Order order = order(orderId);
        Rider rider = rider(riderId);
        if (!rider.tryClaim()) {
            return false;
        }
        if (order.assignRider(riderId)) {
            return true;
        }
        rider.release(); // lost the order race — rider is free again
        return false;
    }

    /** Unassignment + automatic rematch down the strategy's candidate list. */
    public String riderCancels(String orderId, String riderId) {
        Order order = order(orderId);
        if (order.unassignRider(riderId)) {
            rider(riderId).release();
        }
        for (Rider candidate : matching.candidates(riders.values(), restaurant(order.restaurantId()).location())) {
            if (candidate.id().equals(riderId)) {
                continue; // the rider who just bailed is not a candidate
            }
            if (riderAccepts(orderId, candidate.id())) {
                return candidate.id();
            }
        }
        return null; // nobody available — stays unassigned, retried by dispatcher
    }

    /**
     * Per-state rules: free before restaurant accepts; charged after; impossible
     * once picked up. Fee decided from the ACTUAL previous state, atomically.
     */
    public long cancel(String orderId) {
        Order order = order(orderId);
        OrderStatus previous = order.cancelReturningPrevious(); // throws when not cancellable
        long fee = previous == OrderStatus.PLACED ? 0 : lateCancellationFeePaise;
        if (previous == OrderStatus.ACCEPTED || previous == OrderStatus.PREPARING) {
            restaurant(order.restaurantId()).releaseKitchenSlot();
        }
        String riderId = order.rider();
        if (riderId != null && order.unassignRider(riderId)) {
            rider(riderId).release();
        }
        notifyAll(order, previous, OrderStatus.CANCELLED);
        long refund = order.totalPaise() + order.deliveryFeePaise() - fee;
        System.out.println("[refund] " + order.customer() + " gets ₹" + refund / 100
                + (fee > 0 ? " (₹" + fee / 100 + " late-cancel fee withheld)" : " (full refund)"));
        return refund;
    }

    /** Restaurant never accepted in time → auto-cancel, full refund (sweep; prod: scheduler). */
    public int cancelStaleOrders() {
        Instant cutoff = clock.instant().minus(acceptTimeout);
        int cancelled = 0;
        for (Order order : orders.values()) {
            if (order.status() == OrderStatus.PLACED && order.placedAt().isBefore(cutoff)) {
                cancel(order.id());
                cancelled++;
            }
        }
        return cancelled;
    }

    private void transition(Order order, OrderStatus next) {
        OrderStatus previous = order.transitionTo(next);
        notifyAll(order, previous, next);
    }

    private void notifyAll(Order order, OrderStatus from, OrderStatus to) {
        for (OrderObserver observer : observers) {
            try {
                observer.onStatusChange(order, from, to);
            } catch (RuntimeException ignored) {
                // a notification failure never breaks the order flow
            }
        }
    }

    private Order order(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new IllegalArgumentException("no order " + id);
        }
        return order;
    }

    private Rider rider(String id) {
        Rider rider = riders.get(id);
        if (rider == null) {
            throw new IllegalArgumentException("no rider " + id);
        }
        return rider;
    }

    private Restaurant restaurant(String id) {
        Restaurant restaurant = restaurants.get(id);
        if (restaurant == null) {
            throw new IllegalArgumentException("no restaurant " + id);
        }
        return restaurant;
    }
}
