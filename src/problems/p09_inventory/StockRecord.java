package problems.p09_inventory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Optimistic locking in miniature: every mutation is read-validate-CAS with a
 * version bump; a conflict just retries against fresh state. Oversell is
 * impossible because the availability check and the write are one atomic step.
 */
public class StockRecord {
    private final AtomicReference<StockState> state =
            new AtomicReference<>(new StockState(0, 0, 0));

    public StockState snapshot() {
        return state.get();
    }

    boolean tryReserve(int qty) {
        while (true) {
            StockState s = state.get();
            if (s.available() < qty) {
                return false;                                  // oversell prevented
            }
            if (state.compareAndSet(s, new StockState(s.onHand, s.reserved + qty, s.version + 1))) {
                return true;
            }
        }
    }

    void releaseReserved(int qty) {
        mutate(s -> new StockState(s.onHand, s.reserved - qty, s.version + 1));
    }

    void commitReserved(int qty) {
        mutate(s -> new StockState(s.onHand - qty, s.reserved - qty, s.version + 1));
    }

    void addOnHand(int qty) {
        mutate(s -> new StockState(s.onHand + qty, s.reserved, s.version + 1));
    }

    /** Damaged/shrinkage may only consume UNRESERVED stock. */
    boolean tryRemoveAvailable(int qty) {
        while (true) {
            StockState s = state.get();
            if (s.available() < qty) {
                return false;
            }
            if (state.compareAndSet(s, new StockState(s.onHand - qty, s.reserved, s.version + 1))) {
                return true;
            }
        }
    }

    private void mutate(java.util.function.UnaryOperator<StockState> f) {
        while (true) {
            StockState s = state.get();
            if (state.compareAndSet(s, f.apply(s))) {
                return;
            }
        }
    }
}
