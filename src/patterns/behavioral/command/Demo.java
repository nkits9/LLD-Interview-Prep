package patterns.behavioral.command;

public class Demo {
    public static void main(String[] args) {
        EditorBuffer buffer = new EditorBuffer();
        CommandHistory history = new CommandHistory();

        history.run(new InsertCommand(buffer, 0, "hello"));
        history.run(new InsertCommand(buffer, buffer.length(), " world"));
        System.out.println("typed      : '" + buffer.text() + "'");

        history.run(new DeleteCommand(buffer, 5, 6)); // remove " world"
        System.out.println("deleted    : '" + buffer.text() + "'");

        history.undo();
        System.out.println("undo delete: '" + buffer.text() + "'");
        history.redo();
        System.out.println("redo delete: '" + buffer.text() + "'");

        history.undo();                                       // delete undone → "hello world"
        history.run(new InsertCommand(buffer, 0, ">> "));     // new edit clears redo
        System.out.println("new edit   : '" + buffer.text() + "'");

        // Failure path: the redo branch is gone — rejected explicitly.
        try {
            history.redo();
        } catch (IllegalStateException e) {
            System.out.println("rejected   : " + e.getMessage());
        }
    }
}
