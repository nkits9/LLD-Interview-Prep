package problems.p02_amazonlocker;

import java.util.List;

/** Selection will vary (smallest-fit, nearest-to-entrance) — Strategy. */
public interface LockerSelectionStrategy {
    /** Candidate lockers for this size, best first. The service CASes down the list. */
    List<Locker> candidates(List<Locker> lockers, Size packageSize);
}
