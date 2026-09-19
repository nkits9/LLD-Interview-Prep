package patterns.behavioral.memento;

/** Originator: the only class that can create and read snapshots of itself. */
public class Editor {
    private StringBuilder content = new StringBuilder();
    private int cursor;

    public void type(String text) {
        content.insert(cursor, text);
        cursor += text.length();
    }

    public EditorSnapshot snapshot() {
        return new EditorSnapshot(content.toString(), cursor);
    }

    public void restore(EditorSnapshot snapshot) {
        this.content = new StringBuilder(snapshot.content());
        this.cursor = snapshot.cursor();
    }

    public String text() {
        return content.toString();
    }

    public int cursor() {
        return cursor;
    }
}
