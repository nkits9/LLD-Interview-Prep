package problems.p10_splitwise;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    private static String rupees(long paise) {
        String sign = paise < 0 ? "-" : "+";
        long abs = Math.abs(paise);
        return sign + "₹" + abs / 100 + "." + String.format("%02d", abs % 100);
    }

    private static void printBalances(Group group) {
        group.balances().forEach((user, bal) -> System.out.println("   " + user + ": " + rupees(bal)));
        System.out.println("   (sum = " + group.balanceSum() + " paise — must be 0)");
    }

    public static void main(String[] args) throws InterruptedException {
        User alice = new User("u1", "alice");
        User bob = new User("u2", "bob");
        User carol = new User("u3", "carol");
        Group trip = new Group("goa-trip", List.of(alice, bob, carol));

        // THE rounding question: ₹100 ÷ 3 — alice (first) absorbs the extra paisa.
        Expense dinner = trip.addExpense("dinner", alice, 10_000, SplitType.EQUAL,
                List.of(alice, bob, carol), Map.of());
        System.out.println("dinner ₹100 split equally (alice paid):");
        printBalances(trip);

        // Validation BEFORE mutation: bad exact split changes nothing.
        try {
            trip.addExpense("cab", bob, 5_000, SplitType.EXACT,
                    List.of(alice, bob), Map.of(alice, 2_000L, bob, 2_000L)); // sums to 4000 ≠ 5000
        } catch (SplitValidationException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Percent (basis points) and share-based splits.
        trip.addExpense("hotel", bob, 5_000, SplitType.PERCENT,
                List.of(alice, bob, carol), Map.of(alice, 5_000L, bob, 3_000L, carol, 2_000L));
        trip.addExpense("snacks", carol, 999, SplitType.SHARE,
                List.of(alice, bob, carol), Map.of(alice, 2L, bob, 1L, carol, 1L));
        System.out.println("after hotel (50/30/20%) and snacks (2:1:1 of ₹9.99):");
        printBalances(trip);

        // Min-cash-flow settlement plan.
        System.out.println("simplified settle-up:");
        DebtSimplifier.simplify(trip.balances()).forEach(t -> System.out.println("   " + t));

        // Partial settle-up is recorded — and edits recompute around it.
        trip.settle(bob, alice, 2_000);
        System.out.println("bob settles ₹20 to alice:");
        printBalances(trip);

        // Edit recomputes deterministically (dinner becomes ₹90).
        trip.editExpense(dinner.id(), "dinner (corrected)", alice, 9_000, SplitType.EQUAL,
                List.of(alice, bob, carol), Map.of());
        System.out.println("dinner edited to ₹90 — balances recomputed, settlement preserved:");
        printBalances(trip);

        // Concurrency: two threads add to the SAME group — per-group lock keeps sum at zero.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        for (User payer : List.of(alice, bob)) {
            pool.submit(() -> {
                start.await();
                trip.addExpense("parallel-" + payer, payer, 3_333, SplitType.EQUAL,
                        List.of(alice, bob, carol), Map.of());
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        System.out.println("after 2 concurrent expenses: sum = " + trip.balanceSum() + " paise");
    }
}
