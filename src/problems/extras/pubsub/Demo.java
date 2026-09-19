package problems.extras.pubsub;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Broker broker = new Broker();
        broker.createTopic("orders");
        for (int i = 1; i <= 5; i++) {
            broker.publish("orders", "order-" + i);
        }

        // Fan-out: each GROUP independently sees all messages.
        System.out.println("billing poll : " + broker.poll("orders", "billing", 10));
        System.out.println("shipping poll: " + broker.poll("orders", "shipping", 10));

        // At-least-once: no ack → the same batch is redelivered.
        System.out.println("billing again (no ack): " + broker.poll("orders", "billing", 2)
                + "   <- redelivery, consumer must be idempotent");

        // Process 2, ack up to offset 1, then 'crash' — resume exactly after the ack.
        broker.ack("orders", "billing", 1);
        System.out.println("billing after ack(1)  : " + broker.poll("orders", "billing", 10));
        System.out.println("billing lag=" + broker.lag("orders", "billing")
                + ", shipping lag=" + broker.lag("orders", "shipping"));

        // Stale ack never regresses the offset (monotonic commit).
        broker.ack("orders", "billing", 0);
        System.out.println("after stale ack(0)    : " + broker.poll("orders", "billing", 10).get(0));

        // Unknown topic → explicit failure.
        try {
            broker.publish("nope", "x");
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Concurrent publishers: offsets stay unique and contiguous.
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        broker.createTopic("events");
        for (int t = 0; t < 4; t++) {
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 100; i++) {
                    broker.publish("events", Thread.currentThread().getName());
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("publishers did not finish");
        }
        List<Message> all = broker.poll("events", "audit", 1000);
        boolean contiguous = true;
        for (int i = 0; i < all.size(); i++) {
            contiguous &= all.get(i).offset() == i;
        }
        System.out.println("4 publishers x100 -> " + all.size()
                + " messages, offsets contiguous: " + contiguous);
    }
}
