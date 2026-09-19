package problems.extras.chess;

/**
 * Movement rules as enum behaviour — each constant answers "can I attack that
 * square?" (geometry + clear path). Pawns get a separate move rule because
 * their pushes and captures differ; every other piece moves where it attacks.
 */
public enum PieceType {
    KING("K") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            return Math.max(Math.abs(tr - fr), Math.abs(tc - fc)) == 1;
        }
    },
    QUEEN("Q") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            return ROOK.canAttack(b, fr, fc, tr, tc, color)
                    || BISHOP.canAttack(b, fr, fc, tr, tc, color);
        }
    },
    ROOK("R") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            return (fr == tr || fc == tc) && b.pathClear(fr, fc, tr, tc);
        }
    },
    BISHOP("B") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            return Math.abs(tr - fr) == Math.abs(tc - fc) && b.pathClear(fr, fc, tr, tc);
        }
    },
    KNIGHT("N") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            int dr = Math.abs(tr - fr);
            int dc = Math.abs(tc - fc);
            return dr * dc == 2; // (1,2) or (2,1); jumps — no path check
        }
    },
    PAWN("P") {
        @Override
        boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color) {
            int dir = color == Color.WHITE ? 1 : -1;
            return tr - fr == dir && Math.abs(tc - fc) == 1; // diagonal only
        }

        @Override
        boolean canMove(Board b, int fr, int fc, int tr, int tc, Color color) {
            int dir = color == Color.WHITE ? 1 : -1;
            int startRow = color == Color.WHITE ? 1 : 6;
            if (fc == tc && b.at(tr, tc) == null) {              // pushes need empty squares
                if (tr - fr == dir) {
                    return true;
                }
                return fr == startRow && tr - fr == 2 * dir && b.at(fr + dir, fc) == null;
            }
            // captures are the attack squares, and need an enemy piece there
            return canAttack(b, fr, fc, tr, tc, color)
                    && b.at(tr, tc) != null && b.at(tr, tc).color() != color;
        }
    };

    private final String symbol;

    PieceType(String symbol) {
        this.symbol = symbol;
    }

    String symbol() {
        return symbol;
    }

    abstract boolean canAttack(Board b, int fr, int fc, int tr, int tc, Color color);

    /** For every piece except the pawn, moving = attacking. */
    boolean canMove(Board b, int fr, int fc, int tr, int tc, Color color) {
        return canAttack(b, fr, fc, tr, tc, color);
    }
}
