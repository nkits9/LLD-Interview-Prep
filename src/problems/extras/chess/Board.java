package problems.extras.chess;

/** 8x8 grid; row 0 = rank 1 (white's back rank). Pure state + geometry helpers. */
public class Board {
    private final Piece[][] squares = new Piece[8][8];

    Board() {
        PieceType[] backRank = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK};
        for (int c = 0; c < 8; c++) {
            squares[0][c] = new Piece(Color.WHITE, backRank[c]);
            squares[1][c] = new Piece(Color.WHITE, PieceType.PAWN);
            squares[6][c] = new Piece(Color.BLACK, PieceType.PAWN);
            squares[7][c] = new Piece(Color.BLACK, backRank[c]);
        }
    }

    Piece at(int row, int col) {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            return null;
        }
        return squares[row][col];
    }

    void set(int row, int col, Piece piece) {
        squares[row][col] = piece;
    }

    /** Squares strictly between from and to must be empty (sliding pieces). */
    boolean pathClear(int fr, int fc, int tr, int tc) {
        int dr = Integer.signum(tr - fr);
        int dc = Integer.signum(tc - fc);
        int r = fr + dr;
        int c = fc + dc;
        while (r != tr || c != tc) {
            if (squares[r][c] != null) {
                return false;
            }
            r += dr;
            c += dc;
        }
        return true;
    }

    int[] findKing(Color color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                if (piece != null && piece.color() == color && piece.type() == PieceType.KING) {
                    return new int[]{r, c};
                }
            }
        }
        throw new IllegalStateException("no " + color + " king on the board");
    }

    boolean isSquareAttacked(int row, int col, Color by) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = squares[r][c];
                if (piece != null && piece.color() == by
                        && piece.type().canAttack(this, r, c, row, col, by)) {
                    return true;
                }
            }
        }
        return false;
    }
}
