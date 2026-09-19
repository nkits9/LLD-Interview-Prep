package problems.p03_parkinglot;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Don't burn a LARGE spot on a bike: smallest compatible spot type first, then id. */
public class TightestFitAllocation implements SpotAllocationStrategy {
    @Override
    public List<Spot> candidates(List<Spot> spots, VehicleType vehicleType) {
        return spots.stream()
                .filter(spot -> spot.type().canFit(vehicleType))
                .filter(Spot::isFree) // snapshot; the CAS is the real gate
                .sorted(Comparator.comparing((Spot s) -> s.type().ordinal()).thenComparing(Spot::id))
                .collect(Collectors.toList());
    }
}
