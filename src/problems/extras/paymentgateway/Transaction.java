package problems.extras.paymentgateway;

public final class Transaction {
    public enum Status { SUCCEEDED, FAILED }

    private final String id;
    private final String idempotencyKey;
    private final String fromAccount;
    private final String toAccount;
    private final long amountPaise;
    private final Status status;

    Transaction(String id, String idempotencyKey, String fromAccount, String toAccount,
                long amountPaise, Status status) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amountPaise = amountPaise;
        this.status = status;
    }

    public String id() {
        return id;
    }

    public Status status() {
        return status;
    }

    @Override
    public String toString() {
        return id + " " + status + " " + fromAccount + "->" + toAccount
                + " ₹" + amountPaise / 100 + " (key=" + idempotencyKey + ")";
    }
}
