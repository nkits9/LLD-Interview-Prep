package problems.extras.notificationservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Factory/registry: new channel = one register() call, zero edits. */
public class SenderRegistry {
    private final Map<ChannelType, ChannelSender> senders = new ConcurrentHashMap<>();

    public void register(ChannelType type, ChannelSender sender) {
        senders.put(type, sender);
    }

    public ChannelSender senderFor(ChannelType type) {
        ChannelSender sender = senders.get(type);
        if (sender == null) {
            throw new IllegalArgumentException("no sender registered for " + type);
        }
        return sender;
    }
}
