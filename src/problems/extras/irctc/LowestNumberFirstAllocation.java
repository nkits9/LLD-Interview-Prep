package problems.extras.irctc;

import java.util.ArrayList;
import java.util.List;

/** Deterministic default: lowest seat number first — also maximizes seat reuse visibility. */
public class LowestNumberFirstAllocation implements SeatAllocationStrategy {
    @Override
    public List<Seat> allocate(List<Seat> seats, long segmentMask, int count) {
        List<Seat> chosen = new ArrayList<>();
        for (Seat seat : seats) {
            if (chosen.size() == count) {
                break;
            }
            if (seat.isFreeFor(segmentMask)) {
                chosen.add(seat);
            }
        }
        return chosen;
    }
}
