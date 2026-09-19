package patterns.creational.singleton;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        // 100 threads race getInstance(); a correct singleton yields ONE identity.
        Set<Integer> holderIds = ConcurrentHashMap.newKeySet();
        Set<Integer> dclIds = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 100; i++) {
            pool.submit(() -> {
                start.await();                                 // maximize the race
                holderIds.add(System.identityHashCode(AppConfig.getInstance()));
                dclIds.add(System.identityHashCode(DoubleCheckedConfig.getInstance()));
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("threads did not finish");
        }

        System.out.println("holder idiom  : distinct instances = " + holderIds.size());
        System.out.println("double-checked: distinct instances = " + dclIds.size());
        System.out.println("config lookup : " + AppConfig.getInstance().get("region"));
    }
}
