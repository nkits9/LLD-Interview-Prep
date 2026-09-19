package patterns.behavioral.command;

/** Receiver: knows how to mutate text; knows nothing about commands or undo. */
public class EditorBuffer {
    private final StringBuilder text = new StringBuilder();

    void insert(int position, String s) {
        text.insert(position, s);
    }

    /** Returns what was removed — undo needs it. */
    String delete(int position, int length) {
        String removed = text.substring(position, position + length);
        text.delete(position, position + length);
        return removed;
    }

    public String text() {
        return text.toString();
    }

    public int length() {
        return text.length();
    }
}
