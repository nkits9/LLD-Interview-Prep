package patterns.behavioral.strategy;

import java.time.Duration;

/**
 * Context. Depends only on the PricingStrategy interface (DIP) — a new pricing
 * rule is a new class, never an edit here (OCP).
 */
public class BillingService {
    private final PricingStrategy pricing;

    public BillingService(PricingStrategy pricing) {
        this.pricing = pricing;
    }

    public long bill(Duration parked) {
        if (parked.isNegative()) {
            throw new IllegalArgumentException("parked duration cannot be negative: " + parked);
        }
        return pricing.calculateFee(parked);
    }
}
