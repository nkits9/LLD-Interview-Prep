package patterns.structural.composite;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Composite: holds children as a Map (duplicate names impossible, O(1) lookup —
 * never a List) and answers the same interface by recursing.
 */
public class Directory implements FsNode {
    private final String name;
    private final Map<String, FsNode> children = new LinkedHashMap<>();

    public Directory(String name) {
        this.name = name;
    }

    /** add() lives on Directory only — you can't add children to a File (safe variant). */
    public Directory add(FsNode child) {
        if (children.putIfAbsent(child.name(), child) != null) {
            throw new IllegalArgumentException("duplicate name in " + name + ": " + child.name());
        }
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long sizeBytes() {
        // The recursion IS the pattern: a directory's size asks each child,
        // never caring whether it's a file or another whole subtree.
        return children.values().stream().mapToLong(FsNode::sizeBytes).sum();
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + name + "/ (" + sizeBytes() + " B)");
        children.values().forEach(child -> child.print(indent + "  "));
    }
}
