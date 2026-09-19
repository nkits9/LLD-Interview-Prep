package patterns.behavioral.command;

public class InsertCommand implements Command {
    private final EditorBuffer buffer;
    private final int position;
    private final String text;

    public InsertCommand(EditorBuffer buffer, int position, String text) {
        this.buffer = buffer;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        buffer.insert(position, text);
    }

    @Override
    public void undo() {
        buffer.delete(position, text.length()); // exact inverse
    }
}
