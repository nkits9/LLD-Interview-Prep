package problems.p01_connectfour;

/** A player is a class, not a string — identity and piece live together. */
public final class Player {
    private final String name;
    private final Piece piece;

    public Player(String name, Piece piece) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("player name is required");
        }
        this.name = name;
        this.piece = piece;
    }

    public String name() {
        return name;
    }

    public Piece piece() {
        return piece;
    }
}
