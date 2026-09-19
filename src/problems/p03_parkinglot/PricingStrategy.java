package problems.p03_parkinglot;

import java.time.Duration;

/** Pricing varies (hourly, flat, day-cap, EV surcharge) — Strategy. Fees in paise. */
public interface PricingStrategy {
    long fee(VehicleType type, Duration parked);
}
