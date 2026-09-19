package problems.extras.cabbooking;

import java.util.Map;
import java.util.Set;

/** Allowed transitions in one place. */
public enum TripStatus {
    REQUESTED, ONGOING, COMPLETED, CANCELLED;

    private static final Map<TripStatus, Set<TripStatus>> ALLOWED = Map.of(
            REQUESTED, Set.of(ONGOING, CANCELLED),
            ONGOING, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of());

    public boolean canTransitionTo(TripStatus next) {
        return ALLOWED.get(this).contains(next);
    }
}
