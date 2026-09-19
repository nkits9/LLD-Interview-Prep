package problems.p06_movieticketbooking;

/** Unknown, expired, already confirmed/released, or mid-confirmation. */
public class HoldInvalidException extends RuntimeException {
    public HoldInvalidException(String reason) {
        super(reason);
    }
}
