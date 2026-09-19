package problems.extras.leaderboard;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        Leaderboard board = new Leaderboard();

        board.submitScore("asha", 100);
        board.submitScore("bala", 100);   // tied — asha reached 100 FIRST, so asha ranks higher
        board.submitScore("chitra", 80);
        System.out.println("top 3       : " + board.topN(3));
        System.out.println("tie order   : asha #" + board.rankOf("asha")
                + ", bala #" + board.rankOf("bala") + " (earlier submission wins)");

        // Frequent updates: bala improves and takes the lead.
        board.submitScore("bala", 150);
        System.out.println("bala -> 150 : " + board.topN(3) + ", bala #" + board.rankOf("bala"));

        // Unknown player is an explicit failure, not rank 0.
        try {
            board.rankOf("ghost");
        } catch (IllegalArgumentException e) {
            System.out.println("rejected    : " + e.getMessage());
        }

        // Concurrent updates: 4 threads x 500 submissions over 50 players.
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < 4; t++) {
            final int worker = t;
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < 500; i++) {
                    board.submitScore("p" + ((worker * 131 + i * 7) % 50), (worker + 1) * 1000L + i);
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("updates did not finish");
        }
        System.out.println("after 2000 concurrent updates: players=" + board.size()
                + " (expected 53), ranking entries=" + board.rankingSize()
                + " -> structures consistent: " + (board.size() == board.rankingSize()));
        System.out.println("top 3 now   : " + board.topN(3));
    }
}
