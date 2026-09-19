package problems.extras.cardgames;

public final class Card {
    private final Rank rank;
    private final Suit suit;

    Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank rank() {
        return rank;
    }

    @Override
    public String toString() {
        return rank + "-" + suit.name().charAt(0);
    }
}
