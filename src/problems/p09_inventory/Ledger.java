package problems.p09_inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Append-only. Current stock is DERIVABLE from it — replay is the audit (Command/event-sourcing idea). */
public class Ledger {
    private final List<StockEvent> events = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong seq = new AtomicLong();

    void append(MovementType type, String warehouseId, String sku, int qty) {
        events.add(new StockEvent(seq.incrementAndGet(), type, warehouseId, sku, qty));
    }

    public List<StockEvent> all() {
        synchronized (events) {
            return List.copyOf(events);
        }
    }

    /** Fold the events → on-hand. Must equal live state, or somebody bypassed the ledger. */
    public int deriveOnHand(String warehouseId, String sku) {
        int onHand = 0;
        for (StockEvent e : all()) {
            if (!e.warehouseId().equals(warehouseId) || !e.sku().equals(sku)) {
                continue;
            }
            switch (e.type()) {
                case RESTOCK:
                case RETURN:
                    onHand += e.qty();
                    break;
                case COMMIT:
                case DAMAGED:
                    onHand -= e.qty();
                    break;
                default: // RESERVE / RELEASE move the reserved counter, not on-hand
            }
        }
        return onHand;
    }
}
