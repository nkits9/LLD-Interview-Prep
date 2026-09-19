package problems.p02_amazonlocker;

/** Wrong code, already-used code, or expired deposit — never a boolean false. */
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String reason) {
        super("otp rejected: " + reason);
    }
}
