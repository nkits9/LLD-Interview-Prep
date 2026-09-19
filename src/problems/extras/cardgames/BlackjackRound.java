package problems.extras.cardgames;

/**
 * One round: deal 2+2, player hits/stands, dealer plays by strategy, compare.
 * A tiny phase machine guards ordering — no hitting after the round settles.
 */
public class BlackjackRound {
    private enum Phase { PLAYER_TURN, SETTLED }

    private final Deck deck;
    private final DealerStrategy dealerStrategy;
    private final Hand player = new Hand();
    private final Hand dealer = new Hand();
    private Phase phase = Phase.PLAYER_TURN;
    private Outcome outcome;

    public BlackjackRound(Deck deck, DealerStrategy dealerStrategy) {
        this.deck = deck;
        this.dealerStrategy = dealerStrategy;
        player.add(deck.deal());
        dealer.add(deck.deal());
        player.add(deck.deal());
        dealer.add(deck.deal());
        if (player.isBlackjack()) {                       // natural settles immediately
            outcome = dealer.isBlackjack() ? Outcome.PUSH : Outcome.PLAYER_BLACKJACK;
            phase = Phase.SETTLED;
        }
    }

    public void playerHit() {
        requirePlayerTurn();
        player.add(deck.deal());
        if (player.isBust()) {
            outcome = Outcome.DEALER_WIN;                 // bust loses before the dealer acts
            phase = Phase.SETTLED;
        }
    }

    public void playerStand() {
        requirePlayerTurn();
        while (dealerStrategy.hits(dealer)) {             // house plays by its strategy
            dealer.add(deck.deal());
        }
        if (dealer.isBust() || player.bestValue() > dealer.bestValue()) {
            outcome = Outcome.PLAYER_WIN;
        } else if (player.bestValue() < dealer.bestValue()) {
            outcome = Outcome.DEALER_WIN;
        } else {
            outcome = Outcome.PUSH;
        }
        phase = Phase.SETTLED;
    }

    private void requirePlayerTurn() {
        if (phase != Phase.PLAYER_TURN) {
            throw new IllegalStateException("round is settled: " + outcome);
        }
    }

    public Outcome outcome() {
        if (phase != Phase.SETTLED) {
            throw new IllegalStateException("round not finished");
        }
        return outcome;
    }

    public Hand playerHand() {
        return player;
    }

    public Hand dealerHand() {
        return dealer;
    }
}
