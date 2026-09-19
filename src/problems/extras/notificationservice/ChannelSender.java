package problems.extras.notificationservice;

/** One delivery mechanism; throws on failure so decorators can react. */
@FunctionalInterface
public interface ChannelSender {
    void send(Notification notification) throws Exception;
}
