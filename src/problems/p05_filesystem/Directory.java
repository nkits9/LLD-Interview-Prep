package problems.p05_filesystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Composite. Children are a Map (duplicate names unrepresentable, O(1) lookup).
 * Each directory carries its own ReadWriteLock: listings don't block each
 * other, and writers only contend within ONE directory — never the whole tree.
 * The write lock is reentrant, so multi-directory operations (move) can take
 * both parents' locks in a canonical order and still call child ops safely.
 */
public class Directory extends FsNode {
    private final Map<String, FsNode> children = new LinkedHashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    Directory(String name) {
        super(name);
    }

    ReentrantReadWriteLock lock() {
        return lock;
    }

    void addChild(FsNode child) {
        lock.writeLock().lock();
        try {
            if (children.putIfAbsent(child.name(), child) != null) {
                throw new NameConflictException(absolutePath(), child.name());
            }
            child.setParent(this);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void removeChild(FsNode child) {
        lock.writeLock().lock();
        try {
            children.remove(child.name());
            child.setParent(null);
        } finally {
            lock.writeLock().unlock();
        }
    }

    FsNode child(String name) {
        lock.readLock().lock();
        try {
            return children.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return children.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> list() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(children.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Recursive size — the Composite payoff; read locks, so listings proceed in parallel. */
    @Override
    public long sizeBytes() {
        lock.readLock().lock();
        try {
            return children.values().stream().mapToLong(FsNode::sizeBytes).sum();
        } finally {
            lock.readLock().unlock();
        }
    }
}
