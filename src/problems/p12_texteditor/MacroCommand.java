package problems.p12_texteditor;

import java.util.List;

/** Composite: N edits, ONE history entry — undo runs the children in reverse. */
public class MacroCommand implements EditorCommand {
    private final List<EditorCommand> commands;

    public MacroCommand(List<EditorCommand> commands) {
        this.commands = List.copyOf(commands);
    }

    @Override
    public void execute() {
        commands.forEach(EditorCommand::execute);
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
}
