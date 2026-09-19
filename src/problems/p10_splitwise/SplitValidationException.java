package problems.p10_splitwise;

/** Splits must sum to the total — rejected BEFORE any balance is touched. */
public class SplitValidationException extends RuntimeException {
    public SplitValidationException(String reason) {
        super(reason);
    }
}
