package problems.extras.cardgames;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** 52 cards, shuffled with an INJECTED Random — every game is replayable. */
public class Deck {
    private final Deque<Card> cards = new ArrayDeque<>();

    public Deck(Random random) {
        List<Card> all = new ArrayList<>(52);
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                all.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(all, random);
        cards.addAll(all);
    }

    public Card deal() {
        Card card = cards.pollFirst();
        if (card == null) {
            throw new IllegalStateException("deck exhausted — reshuffle the shoe");
        }
        return card;
    }
}
