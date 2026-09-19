package patterns.structural.decorator;

/** Component: decorators implement this AND wrap one of these. */
public interface Notifier {
    void send(String message);
}
