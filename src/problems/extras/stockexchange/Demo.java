package problems.extras.stockexchange;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Exchange exchange = new Exchange();

        // Resting asks. ORD-3 sits BEHIND ORD-1 at the same price (time priority).
        exchange.submit("RELI", Side.SELL, 10_100, 100); // ORD-1: 100 @ ₹101
        exchange.submit("RELI", Side.SELL, 10_200, 50);  // ORD-2:  50 @ ₹102
        exchange.submit("RELI", Side.SELL, 10_100, 30);  // ORD-3:  30 @ ₹101
        OrderBook book = exchange.book("RELI");
        System.out.println("best ask: ₹" + book.bestAskPaise() / 100);

        // Aggressive buy 120 @ ₹102: fills at ₹101 (price improvement),
        // ORD-1 fully before ORD-3 (time priority), ORD-3 partially.
        System.out.println("BUY 120@102 -> " + exchange.submit("RELI", Side.BUY, 10_200, 120));

        // Partial fill + remainder rests as the new best bid.
        System.out.println("BUY 60@101  -> " + exchange.submit("RELI", Side.BUY, 10_100, 60));
        System.out.println("book: bid ₹" + book.bestBidPaise() / 100
                + " / ask ₹" + book.bestAskPaise() / 100 + "   <- spread");

        // Cancel the resting ask.
        System.out.println("cancel ORD-2: " + book.cancel("ORD-2")
                + ", ask now: " + book.bestAskPaise());
        System.out.println("cancel bogus: " + book.cancel("ORD-999"));

        // Seller crossing the bid executes AT THE BID (resting price wins).
        System.out.println("SELL 20@100 -> " + exchange.submit("RELI", Side.SELL, 10_000, 20));

        // Invalid order.
        try {
            exchange.submit("RELI", Side.BUY, 10_000, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Concurrency: 100 buys and 100 sells at one price from two threads.
        // synchronized matching per symbol → exactly 100 shares trade, book ends flat.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        pool.submit(() -> {
            start.await();
            for (int i = 0; i < 100; i++) {
                exchange.submit("TCS", Side.BUY, 10_000, 1);
            }
            return null;
        });
        pool.submit(() -> {
            start.await();
            for (int i = 0; i < 100; i++) {
                exchange.submit("TCS", Side.SELL, 10_000, 1);
            }
            return null;
        });
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race did not finish");
        }
        OrderBook tcs = exchange.book("TCS");
        int traded = tcs.trades().stream().mapToInt(Trade::quantity).sum();
        System.out.println("race: traded " + traded + " shares, book flat: "
                + (tcs.bestBidPaise() == null && tcs.bestAskPaise() == null));
    }
}
