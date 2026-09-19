package problems.p01_connectfour;

import java.util.Optional;

/**
 * Coordinator: turn order and game status only. Board owns state, the rule
 * owns winning — this class never inspects the grid.
 */
public class Game {
    private final Board board;
    private final WinRule rule;
    private final Player[] players;
    private int turn;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Player winner;

    public Game(Board board, WinRule rule, Player one, Player two) {
        if (one.piece() == two.piece()) {
            throw new IllegalArgumentException("players must use different pieces");
        }
        this.board = board;
        this.rule = rule;
        this.players = new Player[]{one, two};
    }

    /** One move: drop → win check around the landed piece → draw check → next turn. */
    public void play(int column) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new GameOverException(status == GameStatus.WON ? "won by " + winner.name() : "draw");
        }
        Player current = players[turn];
        int row = board.drop(column, current.piece()); // throws before any mutation on a bad move
        if (rule.isWin(board, row, column)) {
            status = GameStatus.WON;
            winner = current;
        } else if (board.isFull()) {
            status = GameStatus.DRAW;
        } else {
            turn = 1 - turn;
        }
    }

    public Player currentPlayer() {
        return players[turn];
    }

    public GameStatus status() {
        return status;
    }

    public Optional<Player> winner() {
        return Optional.ofNullable(winner);
    }

    public Board board() {
        return board;
    }
}
