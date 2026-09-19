package problems.extras.cardgames;

import java.util.ArrayList;
import java.util.List;

/** The rule-engine heart: soft-ace valuation — count aces as 1, promote ONE to 11 if it fits. */
public class Hand {
    private final List<Card> cards = new ArrayList<>();

    public void add(Card card) {
        cards.add(card);
    }

    public int bestValue() {
        int sum = 0;
        boolean hasAce = false;
        for (Card card : cards) {
            sum += card.rank().value();
            hasAce |= card.rank() == Rank.ACE;
        }
        return (hasAce && sum + 10 <= 21) ? sum + 10 : sum; // at most ONE ace can be 11
    }

    public boolean isSoft() {
        int hard = cards.stream().mapToInt(c -> c.rank().value()).sum();
        return bestValue() != hard;
    }

    public boolean isBust() {
        return bestValue() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && bestValue() == 21;
    }

    public Card upCard() {
        return cards.get(0);
    }

    @Override
    public String toString() {
        return cards + "=" + bestValue() + (isSoft() ? " (soft)" : "");
    }
}
