package problems.extras.stockexchange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Books are independent — symbols never contend with each other. */
public class Exchange {
    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong();

    public List<Trade> submit(String symbol, Side side, long pricePaise, int quantity) {
        OrderBook book = books.computeIfAbsent(symbol, OrderBook::new);
        return book.submit(side, pricePaise, quantity, "ORD-" + orderSeq.incrementAndGet());
    }

    public OrderBook book(String symbol) {
        OrderBook book = books.get(symbol);
        if (book == null) {
            throw new IllegalArgumentException("no book for " + symbol);
        }
        return book;
    }
}
