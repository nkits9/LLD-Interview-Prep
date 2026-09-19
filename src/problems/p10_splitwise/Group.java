package problems.p10_splitwise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The unit of contention: all methods synchronized — one lock per group, so
 * two groups never contend and within a group balances always stay consistent.
 * Balances are DERIVED: recomputed deterministically from expenses + settlements,
 * which is what makes edit/delete safe.
 */
public class Group {
    private final String id;
    private final List<User> members;
    private final Map<String, Expense> expenses = new LinkedHashMap<>();
    private final List<Settlement> settlements = new ArrayList<>();
    private final Map<User, Long> balances = new HashMap<>(); // +ve = should receive
    private final AtomicLong seq = new AtomicLong();

    public Group(String id, List<User> members) {
        this.id = id;
        this.members = List.copyOf(members);
        members.forEach(m -> balances.put(m, 0L));
    }

    public synchronized Expense addExpense(String description, User paidBy, long totalPaise,
                                           SplitType type, List<User> participants,
                                           Map<User, Long> params) {
        validateMembers(paidBy, participants);
        // Validate FIRST: an invalid split throws before any balance moves.
        Map<User, Long> shares = SplitFactory.of(type).split(totalPaise, participants, params);
        Expense expense = new Expense("EXP-" + seq.incrementAndGet(), description, paidBy,
                totalPaise, shares);
        expenses.put(expense.id(), expense);
        recompute();
        return expense;
    }

    public synchronized void editExpense(String expenseId, String description, User paidBy,
                                         long totalPaise, SplitType type,
                                         List<User> participants, Map<User, Long> params) {
        Expense old = expenseOrThrow(expenseId);
        validateMembers(paidBy, participants);
        Map<User, Long> shares = SplitFactory.of(type).split(totalPaise, participants, params);
        expenses.put(old.id(), new Expense(old.id(), description, paidBy, totalPaise, shares));
        recompute();
    }

    public synchronized void deleteExpense(String expenseId) {
        expenseOrThrow(expenseId);
        expenses.remove(expenseId);
        recompute();
    }

    /** Partial settle-up: recorded, so recomputation can never lose it. */
    public synchronized void settle(User from, User to, long amountPaise) {
        if (amountPaise <= 0) {
            throw new IllegalArgumentException("settle amount must be positive");
        }
        validateMembers(from, List.of(to));
        settlements.add(new Settlement(from, to, amountPaise));
        recompute();
    }

    public synchronized Map<User, Long> balances() {
        return new LinkedHashMap<>(balances);
    }

    /** The invariant to state aloud: balances always sum to zero. */
    public synchronized long balanceSum() {
        return balances.values().stream().mapToLong(Long::longValue).sum();
    }

    /** Deterministic recomputation from source events — the roadmap requirement. */
    private void recompute() {
        balances.replaceAll((u, v) -> 0L);
        for (Expense expense : expenses.values()) {
            balances.merge(expense.paidBy(), expense.totalPaise(), Long::sum);
            expense.shares().forEach((user, share) -> balances.merge(user, -share, Long::sum));
        }
        for (Settlement s : settlements) {
            balances.merge(s.from, s.amountPaise, Long::sum);   // payer's debt shrinks
            balances.merge(s.to, -s.amountPaise, Long::sum);    // receiver is owed less
        }
    }

    private void validateMembers(User payer, List<User> participants) {
        if (!members.contains(payer) || !members.containsAll(participants)) {
            throw new IllegalArgumentException("all users must be members of group " + id);
        }
    }

    private Expense expenseOrThrow(String expenseId) {
        Expense expense = expenses.get(expenseId);
        if (expense == null) {
            throw new IllegalArgumentException("no expense " + expenseId);
        }
        return expense;
    }
}
