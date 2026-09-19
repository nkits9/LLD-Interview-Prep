package problems.p01_connectfour;

public class GameOverException extends IllegalStateException {
    public GameOverException(String detail) {
        super("game is already over: " + detail);
    }
}
