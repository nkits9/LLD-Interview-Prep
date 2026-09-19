package problems.p11_fooddelivery;

import java.util.Collection;
import java.util.List;

/** Matching varies (nearest, rating-weighted, batching) — Strategy. */
public interface RiderMatchingStrategy {
    /** Candidate riders, best first; the service claims them via CAS down the list. */
    List<Rider> candidates(Collection<Rider> riders, int restaurantLocation);
}
