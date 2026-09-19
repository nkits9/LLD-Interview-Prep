package patterns.behavioral.command;

import java.util.ArrayDeque;
import java.util.Deque;

/** Invoker: runs commands and owns the two stacks — the whole undo/redo mechanic. */
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void run(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // a new edit invalidates the redo branch
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("nothing to undo");
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("nothing to redo");
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }
}
