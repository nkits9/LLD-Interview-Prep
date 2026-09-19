package patterns.behavioral.memento;

/**
 * Memento: an immutable snapshot, OPAQUE to everyone but the originator —
 * accessors are package-private, so the caretaker can store it but never peek.
 */
public final class EditorSnapshot {
    private final String content;
    private final int cursor;

    EditorSnapshot(String content, int cursor) {
        this.content = content;
        this.cursor = cursor;
    }

    String content() {
        return content;
    }

    int cursor() {
        return cursor;
    }
}
