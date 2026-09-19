package problems.p12_texteditor;

import java.util.ArrayDeque;
import java.util.Deque;

/** Two stacks + merge-on-push (keystroke grouping) + redo cleared on any new edit. */
public class CommandHistory {
    private final Deque<EditorCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditorCommand> redoStack = new ArrayDeque<>();

    public void run(EditorCommand command) {
        command.execute();
        if (undoStack.isEmpty() || !undoStack.peek().tryMerge(command)) {
            undoStack.push(command);       // not mergeable → its own undo unit
        }
        redoStack.clear();                 // history branched: the redo chain is dead
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("nothing to undo");
        }
        EditorCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("nothing to redo");
        }
        EditorCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}
