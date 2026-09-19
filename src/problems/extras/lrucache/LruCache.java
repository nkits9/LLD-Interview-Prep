package problems.extras.lrucache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The canonical O(1) LRU: HashMap for lookup + hand-rolled doubly-linked list
 * for recency order (head = most recent, tail = eviction candidate).
 *
 * ONE lock, not a ReadWriteLock — an LRU get MUTATES recency order, so there
 * are no read-only operations to parallelize. Scale via sharding (ShardedCache).
 *
 * TTL is lazy: expired entries die when touched (plus evictExpired() for sweeps).
 */
public class LruCache<K, V> implements Cache<K, V> {
    private final class Node {
        final K key;
        V value;
        Instant expiresAt;
        Node prev;
        Node next;

        Node(K key) {
            this.key = key;
        }
    }

    private final int capacity;
    private final Duration ttl;     // null = no expiry
    private final Clock clock;
    private final Map<K, Node> index = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Node head = new Node(null); // sentinels: no null checks in unlink
    private final Node tail = new Node(null);

    public LruCache(int capacity, Duration ttl, Clock clock) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be >= 1");
        }
        this.capacity = capacity;
        this.ttl = ttl;
        this.clock = clock;
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public Optional<V> get(K key) {
        lock.lock();
        try {
            Node node = index.get(key);
            if (node == null) {
                return Optional.empty();
            }
            if (isExpired(node)) {
                unlink(node);
                index.remove(key);
                return Optional.empty();          // lazy expiry
            }
            moveToFront(node);                    // the mutation hiding inside a "read"
            return Optional.of(node.value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        lock.lock();
        try {
            Node node = index.get(key);
            if (node == null) {
                node = new Node(key);
                index.put(key, node);
                linkAtFront(node);
                if (index.size() > capacity) {
                    Node evict = tail.prev;       // least recently used
                    unlink(evict);
                    index.remove(evict.key);
                }
            } else {
                moveToFront(node);
            }
            node.value = value;
            node.expiresAt = ttl == null ? null : clock.instant().plus(ttl);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return index.size();
        } finally {
            lock.unlock();
        }
    }

    /** Optional sweep — lazy expiry means this is hygiene, not correctness. */
    public int evictExpired() {
        lock.lock();
        try {
            int removed = 0;
            for (Node node = head.next; node != tail; ) {
                Node next = node.next;
                if (isExpired(node)) {
                    unlink(node);
                    index.remove(node.key);
                    removed++;
                }
                node = next;
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    private boolean isExpired(Node node) {
        return node.expiresAt != null && clock.instant().isAfter(node.expiresAt);
    }

    private void moveToFront(Node node) {
        unlink(node);
        linkAtFront(node);
    }

    private void linkAtFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void unlink(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
}
