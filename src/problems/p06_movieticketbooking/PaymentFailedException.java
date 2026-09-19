package problems.p06_movieticketbooking;

/** Payment declined — the hold has been released (compensating action). */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String holdId) {
        super("payment failed for hold " + holdId + "; seats released");
    }
}
