package problems.extras.cabbooking;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class NearestDriverMatching implements DriverMatchingStrategy {
    @Override
    public List<Driver> candidates(Collection<Driver> drivers, Location pickup) {
        return drivers.stream()
                .filter(Driver::isAvailable) // snapshot; tryClaim is the real gate
                .sorted(Comparator.comparingInt((Driver d) -> d.location().distanceTo(pickup))
                        .thenComparing(Driver::id))
                .toList();
    }
}
