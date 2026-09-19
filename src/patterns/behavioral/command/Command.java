package patterns.behavioral.command;

/** An action reified as an object — storable, queueable, and reversible. */
public interface Command {
    void execute();

    void undo();
}
