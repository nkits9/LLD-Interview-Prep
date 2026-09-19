package patterns.behavioral.strategy;

import java.time.Duration;

public class Demo {
    public static void main(String[] args) {
        Duration stay = Duration.ofMinutes(150); // 2h30m → 3 started hours

        // Same context, swapped rule — the whole point of the pattern.
        BillingService hourly = new BillingService(new HourlyPricing(5_000, 3_000)); // ₹50 first hr, ₹30 after
        BillingService flat = new BillingService(new FlatRatePricing(10_000));       // ₹100 event parking
        BillingService promo = new BillingService(parked -> 0L);                     // a lambda IS a strategy

        System.out.println("2h30m hourly : " + rupees(hourly.bill(stay)));
        System.out.println("2h30m flat   : " + rupees(flat.bill(stay)));
        System.out.println("2h30m promo  : " + rupees(promo.bill(stay)));

        // Failure path: invalid input is rejected, not silently priced.
        try {
            hourly.bill(Duration.ofMinutes(-5));
        } catch (IllegalArgumentException e) {
            System.out.println("rejected     : " + e.getMessage());
        }
    }

    private static String rupees(long paise) {
        return String.format("₹%d.%02d", paise / 100, paise % 100);
    }
}
