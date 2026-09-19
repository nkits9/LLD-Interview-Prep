package problems.extras.taskscheduler;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Priority queue ordered by next-run-time + a polling step that hands due
 * tasks to a worker pool. tick() IS the polling thread's loop body, driven by
 * an injected clock so everything is testable; production replaces "advance
 * clock + tick" with a condition-wait until the head is due (or a DelayQueue).
 *
 * PriorityQueue is not thread-safe — every queue touch happens under ONE lock,
 * and execution happens OUTSIDE it (never run user code while holding a lock).
 */
public class Scheduler {
    private final PriorityQueue<Task> queue = new PriorityQueue<>(
            Comparator.comparing((Task t) -> t.nextRunAt).thenComparingLong(t -> t.seq));
    private final ReentrantLock lock = new ReentrantLock();
    private final Set<String> activeKeys = ConcurrentHashMap.newKeySet(); // idempotent submission
    private final List<Task> deadLetter = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong seq = new AtomicLong();

    private final ExecutorService workers;
    private final Clock clock;
    private final Duration retryBaseDelay;

    public Scheduler(ExecutorService workers, Clock clock, Duration retryBaseDelay) {
        this.workers = workers;
        this.clock = clock;
        this.retryBaseDelay = retryBaseDelay;
    }

    /** One-shot. Returns false if the key is already known — duplicate submits are no-ops. */
    public boolean schedule(String key, String name, Duration delay, int maxRetries, Runnable action) {
        return enqueueNew(new Task(key, name, action, clock.instant().plus(delay),
                null, maxRetries, seq.incrementAndGet()));
    }

    /** Fixed-rate recurring. */
    public boolean scheduleRecurring(String key, String name, Duration every, Runnable action) {
        return enqueueNew(new Task(key, name, action, clock.instant().plus(every),
                every, 0, seq.incrementAndGet()));
    }

    private boolean enqueueNew(Task task) {
        if (!activeKeys.add(task.key)) {
            return false; // same logical task submitted twice → executed once
        }
        requeue(task);
        return true;
    }

    /** Drain everything due, hand to the pool, return futures (demo joins them). */
    public List<Future<?>> tick() {
        List<Task> due = new ArrayList<>();
        lock.lock();
        try {
            while (!queue.isEmpty() && !queue.peek().nextRunAt.isAfter(clock.instant())) {
                due.add(queue.poll());
            }
        } finally {
            lock.unlock();
        }
        return due.stream().map(task -> workers.submit(() -> run(task))).collect(Collectors.toList());
    }

    private void run(Task task) {
        try {
            task.action.run();
            onSuccess(task);
        } catch (RuntimeException e) {
            onFailure(task, e);
        }
    }

    private void onSuccess(Task task) {
        if (task.interval != null) {                       // recurring: rearm
            task.attempts = 0;
            task.nextRunAt = clock.instant().plus(task.interval);
            requeue(task);
        } else {
            activeKeys.remove(task.key);
        }
    }

    /** Exponential backoff; exhausted retries land in the dead-letter list, never vanish. */
    private void onFailure(Task task, RuntimeException e) {
        task.attempts++;
        if (task.attempts <= task.maxRetries) {
            Duration backoff = retryBaseDelay.multipliedBy(1L << (task.attempts - 1));
            task.nextRunAt = clock.instant().plus(backoff);
            System.out.println("[scheduler] " + task.name + " failed (" + e.getMessage()
                    + "); retry #" + task.attempts + " in " + backoff.toSeconds() + "s");
            requeue(task);
        } else {
            deadLetter.add(task);
            activeKeys.remove(task.key);
            System.out.println("[scheduler] " + task.name + " exhausted retries -> dead letter");
        }
    }

    private void requeue(Task task) {
        lock.lock();
        try {
            queue.add(task);
        } finally {
            lock.unlock();
        }
    }

    public int pendingCount() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public List<Task> deadLetter() {
        return List.copyOf(deadLetter);
    }
}
