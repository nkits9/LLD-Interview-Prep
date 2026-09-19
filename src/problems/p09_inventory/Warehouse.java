package problems.p09_inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Repository: Map<sku, StockRecord>, per-key creation via computeIfAbsent. */
public class Warehouse {
    private final String id;
    private final Map<String, StockRecord> stock = new ConcurrentHashMap<>();

    public Warehouse(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    StockRecord record(String sku) {
        return stock.computeIfAbsent(sku, s -> new StockRecord());
    }

    public int available(String sku) {
        return record(sku).snapshot().available();
    }

    public int onHand(String sku) {
        return record(sku).snapshot().onHand;
    }

    public int reserved(String sku) {
        return record(sku).snapshot().reserved;
    }
}
