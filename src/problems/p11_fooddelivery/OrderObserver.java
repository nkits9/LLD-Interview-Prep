package problems.p11_fooddelivery;

/** Customer, restaurant, and rider each react differently to the same event (Observer). */
@FunctionalInterface
public interface OrderObserver {
    void onStatusChange(Order order, OrderStatus from, OrderStatus to);
}
