package problems.p12_texteditor;

/** Memento: immutable snapshot of content AND cursor; opaque outside the package. */
public final class EditorState {
    private final String content;
    private final int cursor;

    EditorState(String content, int cursor) {
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
