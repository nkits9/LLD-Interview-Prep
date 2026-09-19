package problems.extras.paymentgateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Append-only. Balances are DERIVABLE from it — reconciliation replays and compares. */
public class Ledger {
    private final List<LedgerEntry> entries = Collections.synchronizedList(new ArrayList<>());

    void appendDoubleEntry(String txnId, String fromAccount, String toAccount, long amountPaise) {
        // both legs appended together, under the caller's account locks
        entries.add(new LedgerEntry(txnId, fromAccount, -amountPaise));
        entries.add(new LedgerEntry(txnId, toAccount, amountPaise));
    }

    public List<LedgerEntry> all() {
        synchronized (entries) {
            return List.copyOf(entries);
        }
    }

    public long deriveBalance(String accountId) {
        return all().stream()
                .filter(e -> e.accountId().equals(accountId))
                .mapToLong(LedgerEntry::deltaPaise).sum();
    }
}
