package problems.p03_parkinglot;

import java.time.Duration;
import java.util.Map;

/** Per-type rate, rounded UP per started hour — the rounding rule is explicit, not accidental. */
public class HourlyPricing implements PricingStrategy {
    private final Map<VehicleType, Long> ratePerHourPaise;

    public HourlyPricing(Map<VehicleType, Long> ratePerHourPaise) {
        this.ratePerHourPaise = Map.copyOf(ratePerHourPaise);
    }

    @Override
    public long fee(VehicleType type, Duration parked) {
        Long rate = ratePerHourPaise.get(type);
        if (rate == null) {
            throw new IllegalArgumentException("no rate configured for " + type);
        }
        long startedHours = Math.max(1, (parked.toMinutes() + 59) / 60);
        return startedHours * rate;
    }
}
