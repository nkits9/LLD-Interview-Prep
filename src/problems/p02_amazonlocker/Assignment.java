package problems.p02_amazonlocker;

/** What the courier walks away with: which locker, and the pickup code for the customer. */
public final class Assignment {
    private final String lockerId;
    private final String otpCode;

    Assignment(String lockerId, String otpCode) {
        this.lockerId = lockerId;
        this.otpCode = otpCode;
    }

    public String lockerId() {
        return lockerId;
    }

    public String otpCode() {
        return otpCode;
    }

    @Override
    public String toString() {
        return "locker=" + lockerId + " otp=" + otpCode;
    }
}
