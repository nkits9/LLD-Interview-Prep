package problems.extras.pubsub;

/** Immutable log entry; the offset IS its identity within the topic. */
public final class Message {
    private final long offset;
    private final String payload;

    Message(long offset, String payload) {
        this.offset = offset;
        this.payload = payload;
    }

    public long offset() {
        return offset;
    }

    public String payload() {
        return payload;
    }

    @Override
    public String toString() {
        return "@" + offset + ":" + payload;
    }
}
