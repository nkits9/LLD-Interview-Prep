package problems.p11_fooddelivery;

import java.util.Map;
import java.util.Set;

/** ALL allowed transitions live in this one map — the roadmap's exact requirement. */
public enum OrderStatus {
    PLACED, ACCEPTED, PREPARING, PICKED_UP, DELIVERED, CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PLACED, Set.of(ACCEPTED, CANCELLED),
            ACCEPTED, Set.of(PREPARING, CANCELLED),
            PREPARING, Set.of(PICKED_UP, CANCELLED),
            PICKED_UP, Set.of(DELIVERED),
            DELIVERED, Set.of(),
            CANCELLED, Set.of());

    public boolean canTransitionTo(OrderStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
