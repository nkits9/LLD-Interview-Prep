package problems.extras.pubsub;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Topics + per-(topic, group) committed offsets.
 *  - Every GROUP sees every message (fan-out); consumers WITHIN a group share
 *    the group's offset (queue semantics).
 *  - poll() does NOT advance the offset; ack() does. Crash between them →
 *    redelivery: AT-LEAST-ONCE, so consumers must be idempotent.
 */
public class Broker {
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> committedOffsets = new ConcurrentHashMap<>();

    public void createTopic(String name) {
        topics.putIfAbsent(name, new Topic(name));
    }

    public long publish(String topic, String payload) {
        return topicOrThrow(topic).append(payload);
    }

    /** Read from the group's committed offset; the offset does not move here. */
    public List<Message> poll(String topic, String group, int maxBatch) {
        Topic t = topicOrThrow(topic);
        long committed = offsetFor(topic, group).get();
        return t.read(committed, maxBatch);
    }

    /** Commit everything up to and including {@code upToOffset}. Monotonic — acks never regress. */
    public void ack(String topic, String group, long upToOffset) {
        topicOrThrow(topic);
        AtomicLong committed = offsetFor(topic, group);
        long next = upToOffset + 1;
        while (true) {
            long current = committed.get();
            if (next <= current || committed.compareAndSet(current, next)) {
                return;
            }
        }
    }

    /** How far behind this group is — the ops metric that matters. */
    public long lag(String topic, String group) {
        return topicOrThrow(topic).endOffset() - offsetFor(topic, group).get();
    }

    private AtomicLong offsetFor(String topic, String group) {
        return committedOffsets.computeIfAbsent(topic + "::" + group, k -> new AtomicLong());
    }

    private Topic topicOrThrow(String name) {
        Topic topic = topics.get(name);
        if (topic == null) {
            throw new IllegalArgumentException("no topic '" + name + "'");
        }
        return topic;
    }
}
