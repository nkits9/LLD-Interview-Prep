package problems.extras.chess;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Turn loop + full move legality: geometry (piece type), no capturing your own
 * piece, and the king-safety rule — a move that leaves YOUR king attacked is
 * applied, tested, and rolled back (that's what the Command's undo is for).
 */
public class ChessGame {
    private final Board board = new Board();
    private final Deque<MoveCommand> history = new ArrayDeque<>();
    private Color turn = Color.WHITE;

    /** Algebraic squares: "e2" -> row 1, col 4. */
    public boolean move(String from, String to) {
        int fr = row(from);
        int fc = col(from);
        int tr = row(to);
        int tc = col(to);
        Piece piece = board.at(fr, fc);
        if (piece == null || piece.color() != turn) {
            throw new IllegalMoveException("no " + turn + " piece on " + from);
        }
        Piece target = board.at(tr, tc);
        if (target != null && target.color() == turn) {
            throw new IllegalMoveException("own piece on " + to);
        }
        if (!piece.type().canMove(board, fr, fc, tr, tc, turn)) {
            throw new IllegalMoveException(piece.type() + " cannot move " + from + "->" + to);
        }
        MoveCommand command = new MoveCommand(board, fr, fc, tr, tc);
        command.execute();
        int[] king = board.findKing(turn);
        if (board.isSquareAttacked(king[0], king[1], turn.opponent())) {
            command.undo();                             // try-and-rollback king safety
            throw new IllegalMoveException(from + "->" + to + " leaves your king in check");
        }
        history.push(command);
        turn = turn.opponent();
        // check announcement (mate = check + no legal escape; search all moves — described, not built)
        int[] enemyKing = board.findKing(turn);
        return board.isSquareAttacked(enemyKing[0], enemyKing[1], turn.opponent());
    }

    public void undo() {
        if (history.isEmpty()) {
            throw new IllegalStateException("nothing to undo");
        }
        history.pop().undo();
        turn = turn.opponent();
    }

    public String pieceAt(String square) {
        Piece piece = board.at(row(square), col(square));
        return piece == null ? "-" : piece.toString();
    }

    public Color turn() {
        return turn;
    }

    private int row(String square) {
        return square.charAt(1) - '1';
    }

    private int col(String square) {
        return square.charAt(0) - 'a';
    }
}
