package problems.extras.leaderboard;

/** Immutable ranking row; seq breaks ties — the player who REACHED the score first wins. */
public final class LeaderboardEntry {
    private final String player;
    private final long score;
    private final long seq;

    LeaderboardEntry(String player, long score, long seq) {
        this.player = player;
        this.score = score;
        this.seq = seq;
    }

    public String player() {
        return player;
    }

    public long score() {
        return score;
    }

    long seq() {
        return seq;
    }

    @Override
    public String toString() {
        return player + "=" + score;
    }
}
