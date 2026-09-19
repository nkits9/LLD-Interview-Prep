package patterns.creational.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Registry factory: creation keyed by type, extension = register().
 * A new channel touches no existing code — not even a switch (OCP).
 */
public class NotificationFactory {
    private final Map<ChannelType, Supplier<Notification>> registry = new ConcurrentHashMap<>();

    public void register(ChannelType type, Supplier<Notification> supplier) {
        registry.put(type, supplier);
    }

    public Notification create(ChannelType type) {
        Supplier<Notification> supplier = registry.get(type);
        if (supplier == null) {
            throw new UnsupportedChannelException(type);
        }
        return supplier.get();
    }
}
