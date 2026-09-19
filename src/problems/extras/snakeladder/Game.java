package problems.extras.snakeladder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The game loop owns turns; the Board owns geography; the Dice owns chance;
 * the OvershootPolicy owns the house rule. Rules never live in the loop.
 */
public class Game {
    private final Board board;
    private final Dice dice;
    private final OvershootPolicy overshoot;
    private final Deque<String> turnOrder = new ArrayDeque<>();
    private final Map<String, Integer> positions = new HashMap<>();
    private String winner;

    public Game(Board board, Dice dice, OvershootPolicy overshoot, List<String> players) {
        if (players.size() < 2) {
            throw new IllegalArgumentException("need at least 2 players");
        }
        this.board = board;
        this.dice = dice;
        this.overshoot = overshoot;
        players.forEach(p -> {
            turnOrder.addLast(p);
            positions.put(p, 0); // off-board start
        });
    }

    /** One turn; returns a human-readable trace line. */
    public String playTurn() {
        if (winner != null) {
            throw new IllegalStateException("game over — " + winner + " already won");
        }
        String player = turnOrder.pollFirst();
        int roll = dice.roll();
        int from = positions.get(player);
        int target = overshoot.apply(from + roll, board.size());
        StringBuilder trace = new StringBuilder(player + " rolls " + roll);
        if (target == -1) {
            trace.append(", needs exact — stays at ").append(from);
        } else {
            int landed = board.resolve(target);
            if (board.isSnake(target)) {
                trace.append(", snake ").append(target).append("->").append(landed);
            } else if (board.isLadder(target)) {
                trace.append(", ladder ").append(target).append("->").append(landed);
            } else {
                trace.append(" -> ").append(landed);
            }
            positions.put(player, landed);
            if (landed == board.size()) {
                winner = player;
                trace.append(" — WINS!");
                return trace.toString();
            }
        }
        turnOrder.addLast(player); // rotate only if the game continues
        return trace.toString();
    }

    public String winner() {
        return winner;
    }
}
