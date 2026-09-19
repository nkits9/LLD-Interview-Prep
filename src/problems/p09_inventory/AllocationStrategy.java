package problems.p09_inventory;

import java.util.List;
import java.util.Map;

/** Multi-warehouse split varies (greedy, nearest, cheapest-shipping) — Strategy. */
public interface AllocationStrategy {
    /** Plan warehouseId -> qty covering the full quantity, or throw InsufficientStockException. */
    Map<String, Integer> allocate(List<Warehouse> warehouses, String sku, int qty);
}
