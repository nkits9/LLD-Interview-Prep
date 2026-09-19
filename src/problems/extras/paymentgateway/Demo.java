package problems.extras.paymentgateway;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        WalletService wallet = new WalletService();
        wallet.createAccount("alice");
        wallet.createAccount("bob");

        wallet.topUp("top-1", "alice", 100_000); // ₹1000 enters the system via treasury
        Transaction t1 = wallet.transfer("pay-1", "alice", "bob", 30_000);
        System.out.println("transfer: " + t1);

        // Idempotency: the retry returns the SAME transaction; money moved once.
        Transaction replay = wallet.transfer("pay-1", "alice", "bob", 30_000);
        System.out.println("replay  : " + replay.id() + " == " + t1.id()
                + "; alice ₹" + wallet.balancePaise("alice") / 100
                + ", bob ₹" + wallet.balancePaise("bob") / 100);

        // Failure paths.
        try {
            wallet.transfer("pay-2", "bob", "alice", 500_000);
        } catch (InsufficientBalanceException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            wallet.transfer("pay-3", "alice", "alice", 1_000);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Deadlock test: 200 opposing transfers (alice->bob || bob->alice) in parallel.
        // Without ID-ordered locking this interleaving deadlocks almost instantly.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        for (int i = 0; i < 100; i++) {
            final int n = i;
            pool.submit(() -> {
                start.await();
                wallet.transfer("ab-" + n, "alice", "bob", 100);
                return null;
            });
            pool.submit(() -> {
                start.await();
                wallet.transfer("ba-" + n, "bob", "alice", 100);
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("DEADLOCK: opposing transfers never finished");
        }
        System.out.println("200 opposing transfers finished — no deadlock (lock ordering)");

        // Reconciliation: zero-sum system + ledger replay equals live balances.
        System.out.println("alice ₹" + wallet.balancePaise("alice") / 100
                + ", bob ₹" + wallet.balancePaise("bob") / 100
                + ", treasury ₹" + wallet.balancePaise(WalletService.TREASURY) / 100);
        System.out.println("reconcile (sum==0 && replay==live): " + wallet.reconcile());
        System.out.println("ledger entries: " + wallet.ledger().all().size()
                + " (2 legs per transaction)");
    }
}
