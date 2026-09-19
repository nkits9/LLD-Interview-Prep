package problems.extras.chess;

/** Command: applies a move and can undo it exactly (captured piece restored). */
class MoveCommand {
    private final Board board;
    private final int fr;
    private final int fc;
    private final int tr;
    private final int tc;
    private final Piece moved;
    private final Piece captured;

    MoveCommand(Board board, int fr, int fc, int tr, int tc) {
        this.board = board;
        this.fr = fr;
        this.fc = fc;
        this.tr = tr;
        this.tc = tc;
        this.moved = board.at(fr, fc);
        this.captured = board.at(tr, tc);
    }

    Piece captured() {
        return captured;
    }

    void execute() {
        board.set(tr, tc, moved);
        board.set(fr, fc, null);
    }

    void undo() {
        board.set(fr, fc, moved);
        board.set(tr, tc, captured);
    }
}
