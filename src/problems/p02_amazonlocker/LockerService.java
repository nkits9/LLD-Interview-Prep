package problems.p02_amazonlocker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Orchestrates assign / pickup / expiry. Holds NO lock of its own — all
 * mutual exclusion is per-locker CAS, so parallel couriers only contend
 * when they want the same locker.
 */
public class LockerService {
    private final List<Locker> lockers;
    private final LockerSelectionStrategy selection;
    private final Supplier<String> otpGenerator; // injected: SecureRandom in prod, sequence in tests
    private final Clock clock;                   // injected: expiry is testable
    private final Duration pickupTtl;

    public LockerService(List<Locker> lockers, LockerSelectionStrategy selection,
                         Supplier<String> otpGenerator, Clock clock, Duration pickupTtl) {
        this.lockers = List.copyOf(lockers);
        this.selection = selection;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
        this.pickupTtl = pickupTtl;
    }

    /** CAS down the strategy's candidate list — a lost race just tries the next locker. */
    public Assignment assign(Package pkg) {
        for (Locker locker : selection.candidates(lockers, pkg.size())) {
            Deposit deposit = new Deposit(pkg, new Otp(otpGenerator.get()), clock.instant());
            if (locker.tryDeposit(deposit)) {
                return new Assignment(locker.id(), deposit.otp().code());
            }
        }
        throw new NoLockerAvailableException("no free locker fits a " + pkg.size() + " package");
    }

    /** Single-use OTP; expired deposits are pickup-refused (courier will return them). */
    public Package pickup(String lockerId, String otpAttempt) {
        Locker locker = byId(lockerId);
        Deposit deposit = locker.peek();
        if (deposit == null) {
            throw new InvalidOtpException("locker " + lockerId + " is empty");
        }
        if (isExpired(deposit)) {
            throw new InvalidOtpException("pickup window expired; package awaits courier return");
        }
        deposit.otp().consume(otpAttempt);   // throws on wrong/reused code
        locker.release(deposit);             // atomic return to pool
        return deposit.pkg();
    }

    /** Scheduled sweep (cron/ScheduledExecutorService in prod): frees overdue lockers. */
    public List<Package> expireOverdue() {
        List<Package> returned = new ArrayList<>();
        for (Locker locker : lockers) {
            Deposit deposit = locker.peek();
            if (deposit != null && isExpired(deposit) && locker.release(deposit)) {
                returned.add(deposit.pkg()); // handed back to the courier pile
            }
        }
        return returned;
    }

    private boolean isExpired(Deposit deposit) {
        Instant deadline = deposit.depositedAt().plus(pickupTtl);
        return clock.instant().isAfter(deadline);
    }

    private Locker byId(String lockerId) {
        return lockers.stream().filter(l -> l.id().equals(lockerId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no locker " + lockerId));
    }
}
