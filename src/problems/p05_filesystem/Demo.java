package problems.p05_filesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Demo {
    public static void main(String[] args) throws InterruptedException {
        FileSystem fs = new FileSystem();
        fs.mkdir("/", "docs");
        fs.mkdir("/docs", "notes");
        fs.createFile("/docs", "a.txt", 100);
        fs.createFile("/docs/notes", "b.txt", 50);

        System.out.println("ls /docs            : " + fs.ls("/docs"));
        // Path parsing: '.', '..', trailing and repeated slashes all normalize.
        System.out.println("size /docs/./notes/..//  : " + fs.sizeOf("/docs/./notes/..//") + " B");
        System.out.println("ls /docs/notes/     : " + fs.ls("/docs/notes/"));

        // Failure paths — each a specific exception.
        try {
            fs.createFile("/docs", "a.txt", 10);
        } catch (NameConflictException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            fs.delete("/docs");
        } catch (DirectoryNotEmptyException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            fs.ls("/no/such/dir");
        } catch (PathNotFoundException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Move + rename across directories.
        fs.move("/docs/notes", "/", "archive");
        System.out.println("after move, ls /    : " + fs.ls("/"));
        System.out.println("archive size        : " + fs.sizeOf("/archive") + " B");

        // Cycle prevention: /docs into its own subtree.
        fs.mkdir("/docs", "sub");
        try {
            fs.move("/docs", "/docs/sub", "d");
        } catch (IllegalMoveException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Race: two threads create the same name in the same directory — one wins.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        for (String who : List.of("t1", "t2")) {
            pool.submit(() -> {
                start.await();
                try {
                    fs.createFile("/docs", "same.txt", 1);
                    results.add(who + " -> created");
                } catch (NameConflictException e) {
                    results.add(who + " -> rejected (duplicate)");
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("race demo did not finish");
        }
        Collections.sort(results);
        results.forEach(r -> System.out.println("race: " + r));
    }
}
