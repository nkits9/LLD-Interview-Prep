package problems.extras.irctc;

import java.util.List;

/** Allocation rule varies (lowest-first, window preference, quota pools) — Strategy. */
public interface SeatAllocationStrategy {
    /**
     * Picks up to {@code count} seats that are free across EVERY segment in the
     * mask (one seat must cover the whole journey). Must not mutate seats —
     * occupying happens atomically in TrainRun under its lock.
     */
    List<Seat> allocate(List<Seat> seats, long segmentMask, int count);
}
