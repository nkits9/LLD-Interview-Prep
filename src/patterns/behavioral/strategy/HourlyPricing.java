package patterns.behavioral.strategy;

import java.time.Duration;

/** Base rate for the first hour, then a rate per started hour. Stateless → safely shared across threads. */
public class HourlyPricing implements PricingStrategy {
    private final long firstHourPaise;
    private final long perExtraHourPaise;

    public HourlyPricing(long firstHourPaise, long perExtraHourPaise) {
        this.firstHourPaise = firstHourPaise;
        this.perExtraHourPaise = perExtraHourPaise;
    }

    @Override
    public long calculateFee(Duration parked) {
        long startedHours = Math.max(1, (parked.toMinutes() + 59) / 60); // round UP: a started hour is billed
        return firstHourPaise + (startedHours - 1) * perExtraHourPaise;
    }
}
