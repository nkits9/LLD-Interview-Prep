package patterns.behavioral.strategy;

import java.time.Duration;

/** One price regardless of duration (e.g., event parking). */
public class FlatRatePricing implements PricingStrategy {
    private final long ratePaise;

    public FlatRatePricing(long ratePaise) {
        this.ratePaise = ratePaise;
    }

    @Override
    public long calculateFee(Duration parked) {
        return ratePaise;
    }
}
