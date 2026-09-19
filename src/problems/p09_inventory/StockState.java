package problems.p09_inventory;

/**
 * Immutable snapshot with a version — the optimistic-locking unit. Reserved
 * vs on-hand are TWO numbers: available is derived, never stored.
 */
public final class StockState {
    final int onHand;
    final int reserved;
    final long version;

    StockState(int onHand, int reserved, long version) {
        this.onHand = onHand;
        this.reserved = reserved;
        this.version = version;
    }

    int available() {
        return onHand - reserved;
    }
}
