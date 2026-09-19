package problems.extras.paymentgateway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wallet on a double-entry ledger:
 *  - transfer locks BOTH accounts in ID order (A→B and B→A can never deadlock)
 *  - idempotency key → same Transaction on replay, money moves once
 *  - reconciliation: system-wide sum is zero AND ledger replay equals live balances
 */
public class WalletService {
    public static final String TREASURY = "treasury";

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Transaction> byIdempotencyKey = new ConcurrentHashMap<>();
    private final Ledger ledger = new Ledger();
    private final AtomicLong seq = new AtomicLong();

    public WalletService() {
        accounts.put(TREASURY, new Account(TREASURY, true));
    }

    public void createAccount(String id) {
        accounts.put(id, new Account(id, false));
    }

    public Transaction topUp(String idempotencyKey, String accountId, long amountPaise) {
        return transfer(idempotencyKey, TREASURY, accountId, amountPaise);
    }

    public Transaction withdraw(String idempotencyKey, String accountId, long amountPaise) {
        return transfer(idempotencyKey, accountId, TREASURY, amountPaise);
    }

    public Transaction transfer(String idempotencyKey, String fromId, String toId, long amountPaise) {
        Transaction existing = byIdempotencyKey.get(idempotencyKey);
        if (existing != null) {
            return existing;                                 // replay: money moves ONCE
        }
        if (amountPaise <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("cannot transfer to self");
        }
        Account from = account(fromId);
        Account to = account(toId);

        // global lock order by account id — breaks the A→B / B→A circular wait
        Account first = fromId.compareTo(toId) < 0 ? from : to;
        Account second = first == from ? to : from;
        first.lock().lock();
        second.lock().lock();
        try {
            Transaction replayed = byIdempotencyKey.get(idempotencyKey); // re-check inside locks
            if (replayed != null) {
                return replayed;
            }
            if (!from.canDebit(amountPaise)) {
                throw new InsufficientBalanceException(fromId, amountPaise, from.balancePaiseUnsafe());
            }
            Transaction txn = new Transaction("TXN-" + seq.incrementAndGet(), idempotencyKey,
                    fromId, toId, amountPaise, Transaction.Status.SUCCEEDED);
            from.apply(-amountPaise);
            to.apply(amountPaise);
            ledger.appendDoubleEntry(txn.id(), fromId, toId, amountPaise);
            byIdempotencyKey.put(idempotencyKey, txn);
            return txn;
        } finally {
            second.lock().unlock();
            first.lock().unlock();
        }
    }

    public long balancePaise(String accountId) {
        return account(accountId).balancePaise();
    }

    public Ledger ledger() {
        return ledger;
    }

    /** The auditor's two questions: does the system sum to zero, and does replay match live? */
    public boolean reconcile() {
        long systemSum = accounts.values().stream().mapToLong(Account::balancePaise).sum();
        boolean replayMatches = accounts.keySet().stream()
                .allMatch(id -> ledger.deriveBalance(id) == balancePaise(id));
        return systemSum == 0 && replayMatches;
    }

    private Account account(String id) {
        Account account = accounts.get(id);
        if (account == null) {
            throw new IllegalArgumentException("no account " + id);
        }
        return account;
    }
}
