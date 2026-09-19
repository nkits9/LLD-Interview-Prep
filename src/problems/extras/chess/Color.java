package problems.extras.chess;

public enum Color {
    WHITE, BLACK;

    Color opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
