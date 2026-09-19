package problems.p02_amazonlocker;

import java.time.Instant;

/** Immutable record of what's inside a locker right now. */
public final class Deposit {
    private final Package pkg;
    private final Otp otp;
    private final Instant depositedAt;

    Deposit(Package pkg, Otp otp, Instant depositedAt) {
        this.pkg = pkg;
        this.otp = otp;
        this.depositedAt = depositedAt;
    }

    Package pkg() {
        return pkg;
    }

    Otp otp() {
        return otp;
    }

    Instant depositedAt() {
        return depositedAt;
    }
}
