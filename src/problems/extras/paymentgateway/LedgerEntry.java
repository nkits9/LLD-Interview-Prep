package problems.extras.paymentgateway;

/** One leg of a double entry: every transaction writes TWO of these summing to zero. */
public final class LedgerEntry {
    private final String txnId;
    private final String accountId;
    private final long deltaPaise; // negative = debit, positive = credit

    LedgerEntry(String txnId, String accountId, long deltaPaise) {
        this.txnId = txnId;
        this.accountId = accountId;
        this.deltaPaise = deltaPaise;
    }

    public String accountId() {
        return accountId;
    }

    public long deltaPaise() {
        return deltaPaise;
    }

    @Override
    public String toString() {
        return txnId + " " + accountId + " " + (deltaPaise >= 0 ? "+" : "") + deltaPaise + "p";
    }
}
