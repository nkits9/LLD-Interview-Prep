package problems.p02_amazonlocker;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Don't waste a LARGE locker on a SMALL package. */
public class SmallestFitSelection implements LockerSelectionStrategy {
    @Override
    public List<Locker> candidates(List<Locker> lockers, Size packageSize) {
        return lockers.stream()
                .filter(locker -> locker.size().fits(packageSize))
                .filter(Locker::isFree) // snapshot only — the CAS is the real gate
                .sorted(Comparator.comparing((Locker l) -> l.size().ordinal()).thenComparing(Locker::id))
                .collect(Collectors.toList());
    }
}
