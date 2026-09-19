package problems.p10_splitwise;

/** A recorded settle-up — folded into every recomputation so edits can't lose it. */
final class Settlement {
    final User from;
    final User to;
    final long amountPaise;

    Settlement(User from, User to, long amountPaise) {
        this.from = from;
        this.to = to;
        this.amountPaise = amountPaise;
    }
}
