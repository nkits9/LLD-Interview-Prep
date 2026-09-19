package problems.p12_texteditor;

import java.util.List;

public class Demo {
    private static void show(String label, Editor editor) {
        System.out.println(label + ": '" + editor.text() + "' (cursor " + editor.cursor() + ")");
    }

    public static void main(String[] args) {
        Editor editor = new Editor();

        // 8 keystrokes merge into TWO undo units: "hi " and "there" (space closes a group).
        editor.type("hi there");
        show("typed 8 keys  ", editor);
        editor.undo();
        show("undo (1 word) ", editor);   // cursor restored to the group's start
        editor.redo();
        show("redo          ", editor);

        // Replace with full undo/redo, cursor part of state throughout.
        editor.replace(3, 5, "world");
        show("replace       ", editor);
        editor.undo();
        show("undo replace  ", editor);

        // Redo branch dies on a new edit.
        editor.type("!");
        try {
            editor.redo();
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage() + " (new edit cleared redo)");
        }
        show("after new edit", editor);

        // Macro: wrap the document in quotes — two inserts, ONE undo unit.
        editor.runMacro(List.of(
                new InsertCommand(editor.buffer(), 0, "\""),
                new InsertCommand(editor.buffer(), editor.text().length() + 1, "\"")));
        show("macro (quote) ", editor);
        editor.undo();
        show("undo macro    ", editor);   // both quotes gone in one step

        // Memento checkpoint: content + cursor restored; command stacks cleared.
        editor.checkpoint();
        editor.type(" scratch scratch scratch");
        show("scratch work  ", editor);
        editor.restoreCheckpoint();
        show("restored      ", editor);
        try {
            editor.undo();
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage() + " (checkpoint restore cleared history)");
        }
    }
}
