package problems.p04_elevator;

/** Overload: boarding beyond the limit is rejected, doors stay open. */
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(String reason) {
        super(reason);
    }
}
