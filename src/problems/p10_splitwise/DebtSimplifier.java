package problems.p10_splitwise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Min-cash-flow greedy: repeatedly match the largest creditor with the largest
 * debtor. At most n-1 transfers for n people — the accepted optimal for this
 * problem (true minimum transfer COUNT is NP-hard subset matching; say so).
 */
public final class DebtSimplifier {
    private static final class Entry {
        final User user;
        long amount;

        Entry(User user, long amount) {
            this.user = user;
            this.amount = amount;
        }
    }

    private DebtSimplifier() {
    }

    public static List<Transfer> simplify(Map<User, Long> balances) {
        Comparator<Entry> byAmountDesc = Comparator.<Entry>comparingLong(e -> e.amount).reversed()
                .thenComparing(e -> e.user.id()); // deterministic ties
        PriorityQueue<Entry> creditors = new PriorityQueue<>(byAmountDesc);
        PriorityQueue<Entry> debtors = new PriorityQueue<>(byAmountDesc);
        balances.forEach((user, balance) -> {
            if (balance > 0) {
                creditors.add(new Entry(user, balance));
            } else if (balance < 0) {
                debtors.add(new Entry(user, -balance));
            }
        });

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Entry credit = creditors.poll();
            Entry debt = debtors.poll();
            long amount = Math.min(credit.amount, debt.amount);
            transfers.add(new Transfer(debt.user, credit.user, amount));
            credit.amount -= amount;
            debt.amount -= amount;
            if (credit.amount > 0) {
                creditors.add(credit);
            }
            if (debt.amount > 0) {
                debtors.add(debt);
            }
        }
        return transfers;
    }
}
