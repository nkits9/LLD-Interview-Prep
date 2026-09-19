package problems.p03_parkinglot;

import java.util.List;

/** Allocation varies (tightest fit, nearest to gate, EV-priority) — Strategy. */
public interface SpotAllocationStrategy {
    /** Candidate spots for this vehicle, best first; the service CASes down the list. */
    List<Spot> candidates(List<Spot> spots, VehicleType vehicleType);
}
