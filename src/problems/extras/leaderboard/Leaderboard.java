package problems.extras.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sorted-set + index-map pair (the in-memory Redis ZSET): the skip list keeps
 * rank order, the map finds a player's current entry O(1). An update is
 * remove-old + insert-new — two structures, so writers serialize on one lock;
 * readers stay lock-free on the concurrent skip list.
 */
public class Leaderboard {
    private static final Comparator<LeaderboardEntry> RANKING =
            Comparator.comparingLong(LeaderboardEntry::score).reversed()   // higher score first
                    .thenComparingLong(LeaderboardEntry::seq)              // earlier submission wins ties
                    .thenComparing(LeaderboardEntry::player);

    private final ConcurrentSkipListSet<LeaderboardEntry> ranking = new ConcurrentSkipListSet<>(RANKING);
    private final Map<String, LeaderboardEntry> byPlayer = new ConcurrentHashMap<>();
    private final ReentrantLock writeLock = new ReentrantLock();
    private final AtomicLong seq = new AtomicLong();

    /** Frequent updates are the workload: O(log n) remove + O(log n) insert. */
    public void submitScore(String player, long score) {
        writeLock.lock();
        try {
            LeaderboardEntry old = byPlayer.get(player);
            if (old != null) {
                ranking.remove(old);
            }
            LeaderboardEntry entry = new LeaderboardEntry(player, score, seq.incrementAndGet());
            ranking.add(entry);
            byPlayer.put(player, entry);
        } finally {
            writeLock.unlock();
        }
    }

    public List<LeaderboardEntry> topN(int n) {
        List<LeaderboardEntry> top = new ArrayList<>(n);
        for (LeaderboardEntry entry : ranking) {
            if (top.size() == n) {
                break;
            }
            top.add(entry);
        }
        return top;
    }

    /** O(n) walk — an order-statistic tree or Redis ZRANK makes this O(log n); say it. */
    public int rankOf(String player) {
        LeaderboardEntry mine = byPlayer.get(player);
        if (mine == null) {
            throw new IllegalArgumentException("no scores for " + player);
        }
        int rank = 1;
        for (LeaderboardEntry entry : ranking) {
            if (entry.player().equals(player)) {
                return rank;
            }
            rank++;
        }
        throw new IllegalStateException("index and ranking diverged for " + player);
    }

    public long scoreOf(String player) {
        LeaderboardEntry entry = byPlayer.get(player);
        return entry == null ? 0 : entry.score();
    }

    public int size() {
        return byPlayer.size();
    }

    int rankingSize() {
        return ranking.size();
    }
}
