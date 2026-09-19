package problems.p05_filesystem;

/** Moving a directory into itself/its own descendant would create a cycle. */
public class IllegalMoveException extends RuntimeException {
    public IllegalMoveException(String reason) {
        super(reason);
    }
}
