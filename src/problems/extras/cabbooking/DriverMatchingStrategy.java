package problems.extras.cabbooking;

import java.util.Collection;
import java.util.List;

/** Matching varies (nearest, rating, ETA-model) — Strategy. */
public interface DriverMatchingStrategy {
    List<Driver> candidates(Collection<Driver> drivers, Location pickup);
}
