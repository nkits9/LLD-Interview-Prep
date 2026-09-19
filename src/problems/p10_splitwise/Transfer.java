package problems.p10_splitwise;

/** One suggested payment in the simplified settlement plan. */
public final class Transfer {
    private final User from;
    private final User to;
    private final long amountPaise;

    Transfer(User from, User to, long amountPaise) {
        this.from = from;
        this.to = to;
        this.amountPaise = amountPaise;
    }

    public long amountPaise() {
        return amountPaise;
    }

    @Override
    public String toString() {
        return from + " pays " + to + " ₹" + amountPaise / 100 + "." + String.format("%02d", amountPaise % 100);
    }
}
