package problems.extras.notificationservice;

/** Per-channel outcome — a failing channel is a RESULT, never a thrown fan-out. */
public final class DeliveryResult {
    private final ChannelType channel;
    private final boolean delivered;
    private final String detail;

    static DeliveryResult ok(ChannelType channel) {
        return new DeliveryResult(channel, true, "delivered");
    }

    static DeliveryResult failed(ChannelType channel, String detail) {
        return new DeliveryResult(channel, false, detail);
    }

    private DeliveryResult(ChannelType channel, boolean delivered, String detail) {
        this.channel = channel;
        this.delivered = delivered;
        this.detail = detail;
    }

    public ChannelType channel() {
        return channel;
    }

    public boolean delivered() {
        return delivered;
    }

    @Override
    public String toString() {
        return channel + ": " + (delivered ? "OK" : "FAILED (" + detail + ")");
    }
}
