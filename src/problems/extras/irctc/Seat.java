package problems.extras.irctc;

/**
 * One physical seat for one train run. Occupancy is a bitmask over route
 * segments (bit k = the hop from station k to k+1), so "is this seat free
 * for Delhi->Bhopal?" is one AND — O(1).
 *
 * NOT independently thread-safe: all access is guarded by the owning
 * TrainRun's lock (seat allocation is a cross-seat atomic decision).
 */
public class Seat {
    private final int number;
    private long occupiedSegments;

    Seat(int number) {
        this.number = number;
    }

    public int number() {
        return number;
    }

    boolean isFreeFor(long segmentMask) {
        return (occupiedSegments & segmentMask) == 0;
    }

    void occupy(long segmentMask) {
        if (!isFreeFor(segmentMask)) {
            throw new IllegalStateException("seat " + number + " already occupied on requested segments");
        }
        occupiedSegments |= segmentMask;
    }

    void release(long segmentMask) {
        occupiedSegments &= ~segmentMask;
    }
}
