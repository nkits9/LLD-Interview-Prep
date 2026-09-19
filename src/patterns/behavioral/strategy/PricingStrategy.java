package patterns.behavioral.strategy;

import java.time.Duration;

/** The rule that varies: how a parking stay is priced. */
@FunctionalInterface
public interface PricingStrategy {
    /** Fee in minor units (paise) — money is never a double. */
    long calculateFee(Duration parked);
}
