package problems.p09_inventory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Warehouse whA = new Warehouse("WH-A");
        Warehouse whB = new Warehouse("WH-B");
        InventoryService inventory = new InventoryService(List.of(whA, whB),
                new GreedyAvailabilityAllocation(), 3);
        inventory.subscribe((wh, sku, available) ->
                System.out.println("[alert] LOW STOCK " + sku + " @ " + wh + " (available " + available + ")"));

        inventory.restock("WH-A", "widget", 10);
        inventory.restock("WH-B", "widget", 5);

        // Multi-warehouse all-or-nothing reservation: 12 = 10 from A + 2 from B.
        Reservation res = inventory.reserve("widget", 12);
        System.out.println("reserved 12: " + res.id() + " " + res.allocations());
        System.out.println("WH-A onHand=" + whA.onHand("widget") + " reserved=" + whA.reserved("widget")
                + " | WH-B onHand=" + whB.onHand("widget") + " reserved=" + whB.reserved("widget"));

        // Oversell prevention: only 3 available across both warehouses.
        try {
            inventory.reserve("widget", 5);
        } catch (InsufficientStockException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Commit: on-hand drops, reserved drops — two numbers, moving separately.
        inventory.commit(res.id());
        System.out.println("after commit: WH-A onHand=" + whA.onHand("widget")
                + " | WH-B onHand=" + whB.onHand("widget"));
        try {
            inventory.commit(res.id());
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Returns and damage are their own movement types.
        inventory.recordReturn("WH-A", "widget", 2);
        inventory.recordDamaged("WH-B", "widget", 1);
        try {
            inventory.recordDamaged("WH-B", "widget", 99);
        } catch (InsufficientStockException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Audit: replaying the ledger reproduces live state exactly.
        System.out.println("-- ledger --");
        inventory.ledger().all().forEach(e -> System.out.println("  " + e));
        System.out.println("replay WH-A = " + inventory.ledger().deriveOnHand("WH-A", "widget")
                + ", live = " + whA.onHand("widget"));
        System.out.println("replay WH-B = " + inventory.ledger().deriveOnHand("WH-B", "widget")
                + ", live = " + whB.onHand("widget"));

        // Race: 8 threads reserve 1 gadget each; stock is 5 — EXACTLY 5 must win.
        inventory.restock("WH-A", "gadget", 5);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger wins = new AtomicInteger();
        for (int i = 0; i < 8; i++) {
            pool.submit(() -> {
                start.await();
                try {
                    inventory.reserve("gadget", 1);
                    wins.incrementAndGet();
                } catch (InsufficientStockException ignored) {
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        System.out.println("race: 8 threads, 5 units -> reservations won = " + wins.get()
                + ", reserved = " + whA.reserved("gadget"));
    }
}
