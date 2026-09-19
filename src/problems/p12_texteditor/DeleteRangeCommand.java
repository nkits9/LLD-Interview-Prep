package problems.p12_texteditor;

public class DeleteRangeCommand implements EditorCommand {
    private final EditorBuffer buffer;
    private final int position;
    private final int length;
    private final int previousCursor;
    private String removed; // captured at execute — undo restores exactly this

    public DeleteRangeCommand(EditorBuffer buffer, int position, int length) {
        this.buffer = buffer;
        this.position = position;
        this.length = length;
        this.previousCursor = buffer.cursor();
    }

    @Override
    public void execute() {
        removed = buffer.delete(position, length);
        buffer.setCursor(position);
    }

    @Override
    public void undo() {
        buffer.insert(position, removed);
        buffer.setCursor(previousCursor);
    }
}
