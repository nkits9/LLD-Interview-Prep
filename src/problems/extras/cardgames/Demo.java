package problems.extras.cardgames;

import java.util.Random;

public class Demo {
    public static void main(String[] args) {
        // Soft-ace rule engine, checked directly.
        Hand soft = new Hand();
        soft.add(new Card(Rank.ACE, Suit.SPADES));
        soft.add(new Card(Rank.SIX, Suit.HEARTS));
        System.out.println("A+6        : " + soft);           // 17 (soft)
        soft.add(new Card(Rank.NINE, Suit.CLUBS));
        System.out.println("A+6+9      : " + soft);           // 16 (ace demoted to 1)

        // Seeded rounds — fully reproducible games.
        Deck deck = new Deck(new Random(11));
        BlackjackRound round = new BlackjackRound(deck, DealerStrategy.standOnAll17s());
        System.out.println("player     : " + round.playerHand() + " vs dealer up " + round.dealerHand().upCard());
        // Basic-strategy-lite: hit below 17.
        while (round.playerHand().bestValue() < 17 && !round.playerHand().isBust()) {
            round.playerHit();
            System.out.println("hit ->     : " + round.playerHand());
        }
        if (!round.playerHand().isBust()) {
            round.playerStand();
        }
        System.out.println("dealer     : " + round.dealerHand());
        System.out.println("outcome    : " + round.outcome());

        // Failure path: acting after the round settles.
        try {
            round.playerHit();
        } catch (IllegalStateException e) {
            System.out.println("rejected   : " + e.getMessage());
        }

        // House-rule Strategy: the same soft-17 hand splits the two dealer variants.
        Hand soft17 = new Hand();
        soft17.add(new Card(Rank.ACE, Suit.SPADES));
        soft17.add(new Card(Rank.SIX, Suit.HEARTS));
        System.out.println("soft 17    : standOnAll17s hits? " + DealerStrategy.standOnAll17s().hits(soft17)
                + " | hitSoft17 hits? " + DealerStrategy.hitSoft17().hits(soft17));

        // Three more seeded rounds — outcomes vary, engine is deterministic.
        for (int seed = 1; seed <= 3; seed++) {
            BlackjackRound r = new BlackjackRound(new Deck(new Random(seed)), DealerStrategy.standOnAll17s());
            if (!r.playerHand().isBlackjack()) {
                while (r.playerHand().bestValue() < 17) {
                    r.playerHit();
                }
                if (!r.playerHand().isBust()) {
                    r.playerStand();
                }
            }
            System.out.println("seed " + seed + "     : " + r.playerHand() + " vs "
                    + r.dealerHand() + " -> " + r.outcome());
        }
    }
}
