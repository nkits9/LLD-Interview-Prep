package problems.extras.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An append-only log. Messages are never removed on delivery — consumers just
 * move their offsets. That single decision is what makes fan-out to many
 * groups and replay free.
 */
public class Topic {
    private final String name;
    private final List<Message> log = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    Topic(String name) {
        this.name = name;
    }

    String name() {
        return name;
    }

    long append(String payload) {
        lock.writeLock().lock();
        try {
            long offset = log.size();
            log.add(new Message(offset, payload));
            return offset;
        } finally {
            lock.writeLock().unlock();
        }
    }

    List<Message> read(long fromOffset, int maxBatch) {
        lock.readLock().lock();
        try {
            if (fromOffset >= log.size()) {
                return List.of();
            }
            int from = (int) fromOffset;
            int to = Math.min(from + maxBatch, log.size());
            return List.copyOf(log.subList(from, to));
        } finally {
            lock.readLock().unlock();
        }
    }

    long endOffset() {
        lock.readLock().lock();
        try {
            return log.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
