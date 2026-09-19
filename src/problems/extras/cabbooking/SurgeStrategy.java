package problems.extras.cabbooking;

/** Surge in basis points (10000 = 1.0x) — a pure function of demand vs supply. */
@FunctionalInterface
public interface SurgeStrategy {
    int surgeBasisPoints(int activeTrips, int availableDrivers);
}
