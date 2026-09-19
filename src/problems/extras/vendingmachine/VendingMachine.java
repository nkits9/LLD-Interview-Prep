package problems.extras.vendingmachine;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Context. Inserted coins sit in ESCROW until the sale commits — so a refused
 * sale (out of stock, no change) refunds exactly what was inserted, and the
 * purchase is atomic: item + correct change, or full refund. One transaction
 * at a time: methods are synchronized.
 */
public class VendingMachine {
    private final Map<String, Item> catalog = new HashMap<>();
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<Coin, Integer> coinStock = new EnumMap<>(Coin.class);
    private final List<Coin> escrow = new ArrayList<>();
    private MachineState state = new IdleState();

    public synchronized void load(Item item, int quantity) {
        catalog.put(item.code(), item);
        stock.merge(item.code(), quantity, Integer::sum);
    }

    public synchronized void loadCoins(Coin coin, int count) {
        coinStock.merge(coin, count, Integer::sum);
    }

    // --- public API delegates to the current state ---
    public synchronized void insertCoin(Coin coin) {
        state.insertCoin(this, coin);
    }

    public synchronized Map<Coin, Integer> selectItem(String code) {
        return state.selectItem(this, code);
    }

    public synchronized List<Coin> cancel() {
        return state.cancel(this);
    }

    public synchronized String stateName() {
        return state.name();
    }

    public synchronized long escrowPaise() {
        return escrow.stream().mapToLong(Coin::paise).sum();
    }

    // --- called by states ---
    void transitionTo(MachineState next) {
        this.state = next;
    }

    void escrowCoin(Coin coin) {
        escrow.add(coin);
    }

    List<Coin> refundEscrow() {
        List<Coin> refund = new ArrayList<>(escrow);
        escrow.clear();
        transitionTo(new IdleState());
        return refund;
    }

    /** Validate everything BEFORE mutating anything — refusal leaves a full refund possible. */
    Map<Coin, Integer> vend(String code) {
        Item item = catalog.get(code);
        if (item == null || stock.getOrDefault(code, 0) == 0) {
            throw new OutOfStockException(code);
        }
        long inserted = escrowPaise();
        if (inserted < item.pricePaise()) {
            throw new InsufficientFundsException(item.pricePaise(), inserted);
        }
        // Plan change from (machine stock + escrowed coins) WITHOUT mutating.
        Map<Coin, Integer> availableCoins = new EnumMap<>(coinStock);
        escrow.forEach(c -> availableCoins.merge(c, 1, Integer::sum));
        Map<Coin, Integer> change = planChange(inserted - item.pricePaise(), availableCoins);
        if (change == null) {
            throw new CannotMakeChangeException(inserted - item.pricePaise()); // caller cancels for refund
        }
        // Commit: escrow joins the till, change leaves it, item leaves the rack.
        escrow.forEach(c -> coinStock.merge(c, 1, Integer::sum));
        escrow.clear();
        change.forEach((coin, count) -> coinStock.merge(coin, -count, Integer::sum));
        stock.merge(code, -1, Integer::sum);
        transitionTo(new IdleState());
        return change;
    }

    /** Greedy largest-first — exact for canonical coin systems (say the caveat for arbitrary sets). */
    private Map<Coin, Integer> planChange(long amountPaise, Map<Coin, Integer> available) {
        Map<Coin, Integer> change = new LinkedHashMap<>();
        long remaining = amountPaise;
        Coin[] coins = Coin.values();
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];
            int use = (int) Math.min(remaining / coin.paise(), available.getOrDefault(coin, 0));
            if (use > 0) {
                change.put(coin, use);
                remaining -= use * coin.paise();
            }
        }
        return remaining == 0 ? change : null;
    }
}
