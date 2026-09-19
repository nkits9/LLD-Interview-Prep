package problems.extras.cardgames;

/** House rules vary (stand on all 17s vs hit soft 17) — a real casino Strategy. */
@FunctionalInterface
public interface DealerStrategy {
    boolean hits(Hand dealerHand);

    static DealerStrategy standOnAll17s() {
        return hand -> hand.bestValue() < 17;
    }

    static DealerStrategy hitSoft17() {
        return hand -> hand.bestValue() < 17 || (hand.bestValue() == 17 && hand.isSoft());
    }
}
