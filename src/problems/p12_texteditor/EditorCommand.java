package problems.p12_texteditor;

/** Command with an extra hook: consecutive keystrokes MERGE into one undo unit. */
public interface EditorCommand {
    void execute();

    void undo();

    /** Absorb the (already executed) next command into this undo unit? */
    default boolean tryMerge(EditorCommand next) {
        return false;
    }
}
