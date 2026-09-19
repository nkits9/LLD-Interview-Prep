package problems.p11_fooddelivery;

/** Fee rules vary (distance, surge, subscription) — Strategy. Paise, never double. */
@FunctionalInterface
public interface DeliveryFeeStrategy {
    long feePaise(int distance);
}
