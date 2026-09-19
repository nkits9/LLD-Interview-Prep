package problems.p01_connectfour;

/** Full column and out-of-bounds are explicit failures, never silent no-ops. */
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String reason) {
        super(reason);
    }
}
