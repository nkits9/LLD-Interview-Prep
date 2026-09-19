package problems.p02_amazonlocker;

import java.util.concurrent.atomic.AtomicBoolean;

/** Single-use: consuming is an atomic CAS, so a replayed OTP can never open twice. */
public final class Otp {
    private final String code;
    private final AtomicBoolean used = new AtomicBoolean(false);

    Otp(String code) {
        this.code = code;
    }

    String code() {
        return code;
    }

    /** Valid + first use → consumed; wrong code or second use → rejected. */
    void consume(String attempt) {
        if (!code.equals(attempt)) {
            throw new InvalidOtpException("wrong code");
        }
        if (!used.compareAndSet(false, true)) {
            throw new InvalidOtpException("code already used");
        }
    }
}
