package problems.p09_inventory;

/** Immutable ledger entry — the audit trail IS these events; state is derived. */
public final class StockEvent {
    private final long seq;
    private final MovementType type;
    private final String warehouseId;
    private final String sku;
    private final int qty;

    StockEvent(long seq, MovementType type, String warehouseId, String sku, int qty) {
        this.seq = seq;
        this.type = type;
        this.warehouseId = warehouseId;
        this.sku = sku;
        this.qty = qty;
    }

    public MovementType type() {
        return type;
    }

    public String warehouseId() {
        return warehouseId;
    }

    public String sku() {
        return sku;
    }

    public int qty() {
        return qty;
    }

    @Override
    public String toString() {
        return "#" + seq + " " + type + " " + warehouseId + " " + sku + " x" + qty;
    }
}
