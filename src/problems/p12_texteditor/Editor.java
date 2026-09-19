package problems.p12_texteditor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Facade over buffer + history + checkpoints. Typing goes through per-keystroke
 * commands that MERGE into word-sized undo units.
 */
public class Editor {
    private final EditorBuffer buffer = new EditorBuffer();
    private final CommandHistory history = new CommandHistory();
    private final Deque<EditorState> checkpoints = new ArrayDeque<>();

    /** Simulates real typing: one command per keystroke, merged by the history. */
    public void type(String s) {
        for (char c : s.toCharArray()) {
            history.run(new InsertCommand(buffer, String.valueOf(c)));
        }
    }

    public void deleteRange(int position, int length) {
        history.run(new DeleteRangeCommand(buffer, position, length));
    }

    public void replace(int position, int length, String replacement) {
        history.run(new ReplaceCommand(buffer, position, length, replacement));
    }

    public void runMacro(List<EditorCommand> commands) {
        history.run(new MacroCommand(commands));
    }

    public void undo() {
        history.undo();
    }

    public void redo() {
        history.redo();
    }

    /** Memento checkpoint — content + cursor together. */
    public void checkpoint() {
        checkpoints.push(new EditorState(buffer.text(), buffer.cursor()));
    }

    /** Restoring rewrites the world, so the command stacks (built on the old world) are cleared. */
    public void restoreCheckpoint() {
        if (checkpoints.isEmpty()) {
            throw new IllegalStateException("no checkpoint to restore");
        }
        EditorState state = checkpoints.pop();
        buffer.load(state.content(), state.cursor());
        history.clear();
    }

    public EditorBuffer buffer() {
        return buffer;
    }

    public String text() {
        return buffer.text();
    }

    public int cursor() {
        return buffer.cursor();
    }
}
