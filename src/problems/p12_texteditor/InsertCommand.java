package problems.p12_texteditor;

/** Insert at a position; undo restores text AND cursor (cursor is part of state). */
public class InsertCommand implements EditorCommand {
    private final EditorBuffer buffer;
    private final int position;
    private final int previousCursor;
    private final StringBuilder text; // grows when keystrokes merge

    /** Typed at the cursor. */
    public InsertCommand(EditorBuffer buffer, String text) {
        this(buffer, buffer.cursor(), text);
    }

    /** Placed explicitly (macros). */
    public InsertCommand(EditorBuffer buffer, int position, String text) {
        this.buffer = buffer;
        this.position = position;
        this.previousCursor = buffer.cursor();
        this.text = new StringBuilder(text);
    }

    @Override
    public void execute() {
        buffer.insert(position, text.toString());
        buffer.setCursor(position + text.length());
    }

    @Override
    public void undo() {
        buffer.delete(position, text.length());
        buffer.setCursor(previousCursor);
    }

    /**
     * Keystroke grouping: a following insert that continues exactly where this
     * one ends joins this undo unit; a space ENDS the group (undo works per word).
     */
    @Override
    public boolean tryMerge(EditorCommand next) {
        if (!(next instanceof InsertCommand)) {
            return false;
        }
        InsertCommand incoming = (InsertCommand) next;
        boolean contiguous = incoming.position == this.position + this.text.length();
        boolean groupOpen = text.length() == 0 || text.charAt(text.length() - 1) != ' ';
        if (contiguous && groupOpen) {
            text.append(incoming.text);
            return true;
        }
        return false;
    }
}
