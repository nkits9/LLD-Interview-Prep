package problems.extras.cabbooking;

/** Fare rule varies — Strategy; paise, never double. */
@FunctionalInterface
public interface FareStrategy {
    long farePaise(int distanceKm, int surgeBasisPoints);
}
