package patterns.behavioral.command;

public class DeleteCommand implements Command {
    private final EditorBuffer buffer;
    private final int position;
    private final int length;
    private String removed; // captured on execute — undo must restore exactly this

    public DeleteCommand(EditorBuffer buffer, int position, int length) {
        this.buffer = buffer;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        removed = buffer.delete(position, length);
    }

    @Override
    public void undo() {
        buffer.insert(position, removed);
    }
}
