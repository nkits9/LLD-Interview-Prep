package patterns.behavioral.memento;

public class Demo {
    public static void main(String[] args) {
        Editor editor = new Editor();
        History history = new History();

        history.save(editor.snapshot());          // checkpoint before each edit
        editor.type("hello");
        history.save(editor.snapshot());
        editor.type(" world");
        System.out.println("typed   : '" + editor.text() + "' (cursor " + editor.cursor() + ")");

        editor.restore(history.pop());            // undo " world"
        System.out.println("restore : '" + editor.text() + "' (cursor " + editor.cursor() + ")");

        editor.restore(history.pop());            // undo "hello"
        System.out.println("restore : '" + editor.text() + "' (cursor " + editor.cursor() + ")");

        // Failure path: no snapshots left — rejected explicitly.
        try {
            editor.restore(history.pop());
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
