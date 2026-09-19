package problems.extras.paymentgateway;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Balance guarded by the account's own lock; multi-account operations acquire
 * locks in ID order. The treasury account may go negative — it represents
 * money entering/leaving the closed system, which keeps the global sum at zero.
 */
public class Account {
    private final String id;
    private final boolean treasury;
    private final ReentrantLock lock = new ReentrantLock();
    private long balancePaise;

    Account(String id, boolean treasury) {
        this.id = id;
        this.treasury = treasury;
    }

    public String id() {
        return id;
    }

    ReentrantLock lock() {
        return lock;
    }

    boolean canDebit(long amountPaise) {
        return treasury || balancePaise >= amountPaise;
    }

    void apply(long deltaPaise) {
        balancePaise += deltaPaise;
    }

    public long balancePaise() {
        lock.lock();
        try {
            return balancePaise;
        } finally {
            lock.unlock();
        }
    }

    /** Only call while holding this account's lock. */
    long balancePaiseUnsafe() {
        return balancePaise;
    }
}
