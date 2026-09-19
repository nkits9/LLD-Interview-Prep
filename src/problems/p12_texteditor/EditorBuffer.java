package problems.p12_texteditor;

/**
 * Receiver: text + cursor. A StringBuilder keeps the interview honest;
 * name piece table / gap buffer for large-document efficiency (README §8).
 */
public class EditorBuffer {
    private StringBuilder text = new StringBuilder();
    private int cursor;

    void insert(int position, String s) {
        text.insert(position, s);
    }

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

    public int cursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        if (cursor < 0 || cursor > text.length()) {
            throw new IllegalArgumentException("cursor out of bounds: " + cursor);
        }
        this.cursor = cursor;
    }

    void load(String content, int cursorPosition) {
        this.text = new StringBuilder(content);
        this.cursor = cursorPosition;
    }
}
