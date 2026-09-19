package problems.p11_fooddelivery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class NearestRiderMatching implements RiderMatchingStrategy {
    @Override
    public List<Rider> candidates(Collection<Rider> riders, int restaurantLocation) {
        return riders.stream()
                .filter(Rider::isAvailable) // snapshot; the tryClaim CAS is the real gate
                .sorted(Comparator.comparingInt((Rider r) -> Math.abs(r.location() - restaurantLocation))
                        .thenComparing(Rider::id))
                .toList();
    }
}
