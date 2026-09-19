package problems.p08_ratelimiter;

/** Allow/deny plus retry-after — the 429 header, computed not guessed. */
public final class Decision {
    private final boolean allowed;
    private final long retryAfterMillis;

    static Decision allow() {
        return new Decision(true, 0);
    }

    static Decision deny(long retryAfterMillis) {
        return new Decision(false, retryAfterMillis);
    }

    private Decision(boolean allowed, long retryAfterMillis) {
        this.allowed = allowed;
        this.retryAfterMillis = retryAfterMillis;
    }

    public boolean allowed() {
        return allowed;
    }

    public long retryAfterMillis() {
        return retryAfterMillis;
    }

    @Override
    public String toString() {
        return allowed ? "ALLOW" : "DENY (retry after " + retryAfterMillis + "ms)";
    }
}
