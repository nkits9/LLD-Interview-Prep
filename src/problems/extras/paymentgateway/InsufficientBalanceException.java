package problems.extras.paymentgateway;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String accountId, long requested, long available) {
        super(accountId + " has " + available + "p, cannot debit " + requested + "p");
    }
}
