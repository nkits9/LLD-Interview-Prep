package problems.extras.stockexchange;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * One symbol's book: TreeMap price levels, FIFO deque per level.
 *  - Price priority: best level first (bids descending, asks ascending).
 *  - Time priority: FIFO within a level.
 *  - Executions price at the RESTING order's limit.
 * All entry points are synchronized: matching for a symbol is SINGLE-THREADED
 * by design — real exchanges do exactly this (one matching thread per symbol).
 */
public class OrderBook {
    private final String symbol;
    private final TreeMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<Long, Deque<Order>> asks = new TreeMap<>();
    private final List<Trade> trades = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong();

    public OrderBook(String symbol) {
        this.symbol = symbol;
    }

    public synchronized List<Trade> submit(Side side, long pricePaise, int quantity, String orderId) {
        Order order = new Order(orderId, side, pricePaise, quantity, seq.incrementAndGet());
        List<Trade> executions = new ArrayList<>();
        TreeMap<Long, Deque<Order>> opposite = (side == Side.BUY) ? asks : bids;

        while (!order.isFilled() && !opposite.isEmpty() && crosses(order, opposite.firstKey())) {
            Deque<Order> level = opposite.firstEntry().getValue();
            Order resting = level.peekFirst();                 // time priority within the level
            int qty = Math.min(order.remaining(), resting.remaining());
            long executionPrice = resting.pricePaise();        // resting order sets the price
            order.fill(qty);
            resting.fill(qty);
            executions.add(side == Side.BUY
                    ? new Trade(order.id(), resting.id(), executionPrice, qty)
                    : new Trade(resting.id(), order.id(), executionPrice, qty));
            if (resting.isFilled()) {
                level.pollFirst();
            }
            if (level.isEmpty()) {
                opposite.pollFirstEntry();
            }
        }
        if (!order.isFilled()) {                               // remainder rests in the book
            TreeMap<Long, Deque<Order>> own = (side == Side.BUY) ? bids : asks;
            own.computeIfAbsent(pricePaise, p -> new ArrayDeque<>()).addLast(order);
        }
        trades.addAll(executions);
        return executions;
    }

    private boolean crosses(Order incoming, long bestOppositePrice) {
        return incoming.side() == Side.BUY
                ? bestOppositePrice <= incoming.pricePaise()
                : bestOppositePrice >= incoming.pricePaise();
    }

    public synchronized boolean cancel(String orderId) {
        for (TreeMap<Long, Deque<Order>> bookSide : List.of(bids, asks)) {
            for (Map.Entry<Long, Deque<Order>> level : bookSide.entrySet()) {
                if (level.getValue().removeIf(o -> o.id().equals(orderId))) {
                    if (level.getValue().isEmpty()) {
                        bookSide.remove(level.getKey());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized Long bestBidPaise() {
        return bids.isEmpty() ? null : bids.firstKey();
    }

    public synchronized Long bestAskPaise() {
        return asks.isEmpty() ? null : asks.firstKey();
    }

    public synchronized List<Trade> trades() {
        return List.copyOf(trades);
    }

    public String symbol() {
        return symbol;
    }
}
