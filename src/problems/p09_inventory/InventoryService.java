package problems.p09_inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orchestrates reserve → commit/release across warehouses. Every change goes
 * through the ledger — current stock is derivable, audit is free.
 */
public class InventoryService {
    private final List<Warehouse> warehouses;
    private final AllocationStrategy allocation;
    private final Ledger ledger = new Ledger();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final List<LowStockObserver> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();
    private final int lowStockThreshold;

    public InventoryService(List<Warehouse> warehouses, AllocationStrategy allocation,
                            int lowStockThreshold) {
        this.warehouses = List.copyOf(warehouses);
        this.allocation = allocation;
        this.lowStockThreshold = lowStockThreshold;
    }

    public Ledger ledger() {
        return ledger;
    }

    public void subscribe(LowStockObserver observer) {
        observers.add(observer);
    }

    public void restock(String warehouseId, String sku, int qty) {
        warehouse(warehouseId).record(sku).addOnHand(qty);
        ledger.append(MovementType.RESTOCK, warehouseId, sku, qty);
    }

    /**
     * All-or-nothing across warehouses: plan from a snapshot, then CAS-reserve
     * per warehouse; if any leg loses a race, roll back the earlier legs
     * (compensating actions) and fail cleanly.
     */
    public Reservation reserve(String sku, int qty) {
        Map<String, Integer> plan = allocation.allocate(warehouses, sku, qty);
        List<Map.Entry<String, Integer>> done = new ArrayList<>();
        for (Map.Entry<String, Integer> leg : plan.entrySet()) {
            if (warehouse(leg.getKey()).record(sku).tryReserve(leg.getValue())) {
                done.add(leg);
                ledger.append(MovementType.RESERVE, leg.getKey(), sku, leg.getValue());
            } else {
                for (Map.Entry<String, Integer> rollback : done) {  // compensate
                    warehouse(rollback.getKey()).record(sku).releaseReserved(rollback.getValue());
                    ledger.append(MovementType.RELEASE, rollback.getKey(), sku, rollback.getValue());
                }
                throw new InsufficientStockException("lost reservation race for " + sku + "; retry");
            }
        }
        Reservation reservation = new Reservation("RES-" + seq.incrementAndGet(), sku, plan);
        reservations.put(reservation.id(), reservation);
        plan.keySet().forEach(wh -> checkLowStock(wh, sku));
        return reservation;
    }

    /** Reserved → shipped: on-hand and reserved both drop. */
    public void commit(String reservationId) {
        Reservation reservation = reservationOrThrow(reservationId);
        reservation.transition(Reservation.Status.COMMITTED);
        reservation.allocations().forEach((wh, qty) -> {
            warehouse(wh).record(reservation.sku()).commitReserved(qty);
            ledger.append(MovementType.COMMIT, wh, reservation.sku(), qty);
            checkLowStock(wh, reservation.sku());
        });
    }

    public void release(String reservationId) {
        Reservation reservation = reservationOrThrow(reservationId);
        reservation.transition(Reservation.Status.RELEASED);
        reservation.allocations().forEach((wh, qty) -> {
            warehouse(wh).record(reservation.sku()).releaseReserved(qty);
            ledger.append(MovementType.RELEASE, wh, reservation.sku(), qty);
        });
    }

    public void recordReturn(String warehouseId, String sku, int qty) {
        warehouse(warehouseId).record(sku).addOnHand(qty);
        ledger.append(MovementType.RETURN, warehouseId, sku, qty);
    }

    public void recordDamaged(String warehouseId, String sku, int qty) {
        if (!warehouse(warehouseId).record(sku).tryRemoveAvailable(qty)) {
            throw new InsufficientStockException(
                    "cannot write off " + qty + " of " + sku + ": would eat reserved stock");
        }
        ledger.append(MovementType.DAMAGED, warehouseId, sku, qty);
        checkLowStock(warehouseId, sku);
    }

    private void checkLowStock(String warehouseId, String sku) {
        int available = warehouse(warehouseId).available(sku);
        if (available <= lowStockThreshold) {
            observers.forEach(o -> o.onLowStock(warehouseId, sku, available));
        }
    }

    private Warehouse warehouse(String id) {
        return warehouses.stream().filter(w -> w.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no warehouse " + id));
    }

    private Reservation reservationOrThrow(String id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) {
            throw new IllegalArgumentException("no reservation " + id);
        }
        return reservation;
    }
}
