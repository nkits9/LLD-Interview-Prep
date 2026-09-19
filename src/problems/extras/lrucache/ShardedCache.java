package problems.extras.lrucache;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

/**
 * Lock striping by composition: N independent LRU shards, key-hash picks one.
 * Contention drops ~N-fold; the trade is that LRU order (and capacity) is
 * per-shard, not global — say it.
 */
public class ShardedCache<K, V> implements Cache<K, V> {
    private final LruCache<K, V>[] shards;

    @SuppressWarnings("unchecked")
    public ShardedCache(int shardCount, int capacityPerShard, Duration ttl, Clock clock) {
        shards = new LruCache[shardCount];
        for (int i = 0; i < shardCount; i++) {
            shards[i] = new LruCache<>(capacityPerShard, ttl, clock);
        }
    }

    private LruCache<K, V> shard(K key) {
        int h = key.hashCode();
        h ^= (h >>> 16); // spread, same idea as ConcurrentHashMap
        return shards[Math.floorMod(h, shards.length)];
    }

    @Override
    public Optional<V> get(K key) {
        return shard(key).get(key);
    }

    @Override
    public void put(K key, V value) {
        shard(key).put(key, value);
    }

    @Override
    public int size() {
        int total = 0;
        for (LruCache<K, V> shard : shards) {
            total += shard.size();
        }
        return total;
    }
}
