package problems.extras.notificationservice;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Fan-out: one notification → the user's channels IN PARALLEL, each with its
 * own timeout; per-channel failures become results, never break the others.
 */
public class NotificationService {
    private final SenderRegistry registry;
    private final ExecutorService executor;
    private final Map<String, Set<ChannelType>> preferences = new ConcurrentHashMap<>();

    public NotificationService(SenderRegistry registry, ExecutorService executor) {
        this.registry = registry;
        this.executor = executor;
    }

    public void setPreferences(String userId, Set<ChannelType> channels) {
        preferences.put(userId, Set.copyOf(channels));
    }

    public List<DeliveryResult> notify(String userId, String message) {
        Notification notification = new Notification(userId, message);
        Set<ChannelType> channels = preferences.getOrDefault(userId, Set.of());
        List<CompletableFuture<DeliveryResult>> futures = channels.stream()
                .map(channel -> CompletableFuture.supplyAsync(
                                () -> dispatch(channel, notification), executor)
                        .completeOnTimeout(DeliveryResult.failed(channel, "timed out"),
                                2, TimeUnit.SECONDS))
                .collect(Collectors.toList());
        return futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparing(DeliveryResult::channel))
                .collect(Collectors.toList());
    }

    private DeliveryResult dispatch(ChannelType channel, Notification notification) {
        try {
            registry.senderFor(channel).send(notification);
            return DeliveryResult.ok(channel);
        } catch (Exception e) {
            return DeliveryResult.failed(channel, e.getMessage()); // isolation
        }
    }
}
