package problems.extras.chess;

/**
 * Composition over inheritance: a piece IS color + type; behaviour lives on
 * the type enum. No King-extends-Piece hierarchy to maintain.
 */
public final class Piece {
    private final Color color;
    private final PieceType type;

    Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    Color color() {
        return color;
    }

    PieceType type() {
        return type;
    }

    @Override
    public String toString() {
        return (color == Color.WHITE ? "w" : "b") + type.symbol();
    }
}
