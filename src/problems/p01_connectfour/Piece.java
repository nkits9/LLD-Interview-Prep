package problems.p01_connectfour;

public enum Piece {
    RED('R'), YELLOW('Y');

    private final char symbol;

    Piece(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }
}
