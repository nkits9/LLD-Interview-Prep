package problems.extras.irctc;

/** Confirmed-only booking: not enough seats free across the requested segments. */
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String reason) {
        super(reason);
    }
}
