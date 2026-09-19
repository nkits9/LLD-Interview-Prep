package problems.p12_texteditor;

public class ReplaceCommand implements EditorCommand {
    private final EditorBuffer buffer;
    private final int position;
    private final int length;
    private final String replacement;
    private final int previousCursor;
    private String removed;

    public ReplaceCommand(EditorBuffer buffer, int position, int length, String replacement) {
        this.buffer = buffer;
        this.position = position;
        this.length = length;
        this.replacement = replacement;
        this.previousCursor = buffer.cursor();
    }

    @Override
    public void execute() {
        removed = buffer.delete(position, length);
        buffer.insert(position, replacement);
        buffer.setCursor(position + replacement.length());
    }

    @Override
    public void undo() {
        buffer.delete(position, replacement.length());
        buffer.insert(position, removed);
        buffer.setCursor(previousCursor);
    }
}
