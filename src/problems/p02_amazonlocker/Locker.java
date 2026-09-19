package problems.p02_amazonlocker;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The unit of concurrency: occupancy is an AtomicReference, so claiming is a
 * CAS — two couriers racing for the same locker can never both win, and the
 * location is NEVER locked as a whole.
 */
public class Locker {
    private final String id;
    private final Size size;
    private final AtomicReference<Deposit> current = new AtomicReference<>();

    public Locker(String id, Size size) {
        this.id = id;
        this.size = size;
    }

    public String id() {
        return id;
    }

    public Size size() {
        return size;
    }

    public boolean isFree() {
        return current.get() == null;
    }

    /** Atomic claim: false = somebody else won the race. */
    boolean tryDeposit(Deposit deposit) {
        return size.fits(deposit.pkg().size()) && current.compareAndSet(null, deposit);
    }

    Deposit peek() {
        return current.get();
    }

    /** Atomic return-to-pool: frees only if still holding this exact deposit. */
    boolean release(Deposit expected) {
        return current.compareAndSet(expected, null);
    }
}
