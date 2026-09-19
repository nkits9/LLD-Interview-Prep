package problems.p09_inventory;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fewest shipments: drain the fullest warehouse first. Plan from a snapshot — the CAS is the gate. */
public class GreedyAvailabilityAllocation implements AllocationStrategy {
    @Override
    public Map<String, Integer> allocate(List<Warehouse> warehouses, String sku, int qty) {
        Map<String, Integer> plan = new LinkedHashMap<>();
        int remaining = qty;
        List<Warehouse> byAvailability = warehouses.stream()
                .sorted(Comparator.comparingInt((Warehouse w) -> w.available(sku)).reversed()
                        .thenComparing(Warehouse::id))
                .toList();
        for (Warehouse warehouse : byAvailability) {
            if (remaining == 0) {
                break;
            }
            int take = Math.min(remaining, warehouse.available(sku));
            if (take > 0) {
                plan.put(warehouse.id(), take);
                remaining -= take;
            }
        }
        if (remaining > 0) {
            throw new InsufficientStockException(sku, qty, qty - remaining);
        }
        return plan;
    }
}
