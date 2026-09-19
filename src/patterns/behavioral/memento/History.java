package patterns.behavioral.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/** Caretaker: stores snapshots, never looks inside them. */
public class History {
    private final Deque<EditorSnapshot> stack = new ArrayDeque<>();

    public void save(EditorSnapshot snapshot) {
        stack.push(snapshot);
    }

    public EditorSnapshot pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("nothing to restore");
        }
        return stack.pop();
    }
}
