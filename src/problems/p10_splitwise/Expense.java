package problems.p10_splitwise;

import java.util.Map;

/** Immutable: an edit REPLACES the expense and balances are recomputed. */
public final class Expense {
    private final String id;
    private final String description;
    private final User paidBy;
    private final long totalPaise;
    private final Map<User, Long> shares;

    Expense(String id, String description, User paidBy, long totalPaise, Map<User, Long> shares) {
        this.id = id;
        this.description = description;
        this.paidBy = paidBy;
        this.totalPaise = totalPaise;
        this.shares = Map.copyOf(shares);
    }

    public String id() {
        return id;
    }

    public String description() {
        return description;
    }

    User paidBy() {
        return paidBy;
    }

    long totalPaise() {
        return totalPaise;
    }

    Map<User, Long> shares() {
        return shares;
    }
}
