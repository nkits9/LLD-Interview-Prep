package problems.extras.chess;

public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException(String reason) {
        super(reason);
    }
}
