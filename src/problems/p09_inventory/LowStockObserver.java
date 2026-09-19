package problems.p09_inventory;

/** Alerts on low availability — replenishment systems subscribe (Observer). */
@FunctionalInterface
public interface LowStockObserver {
    void onLowStock(String warehouseId, String sku, int available);
}
