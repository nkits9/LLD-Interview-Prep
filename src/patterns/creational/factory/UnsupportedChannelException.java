package patterns.creational.factory;

/** Specific failure for an unknown type — never return null from a factory. */
public class UnsupportedChannelException extends IllegalArgumentException {
    public UnsupportedChannelException(ChannelType type) {
        super("no notification channel registered for " + type);
    }
}
