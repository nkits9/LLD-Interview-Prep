package problems.p05_filesystem;

/** Composite node: File and Directory share this, so traversal is one code path. */
public abstract class FsNode {
    private String name;
    private Directory parent; // null only for root

    protected FsNode(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    void rename(String newName) {
        this.name = newName;
    }

    public Directory parent() {
        return parent;
    }

    void setParent(Directory parent) {
        this.parent = parent;
    }

    public String absolutePath() {
        if (parent == null) {
            return "/";
        }
        String parentPath = parent.absolutePath();
        return parentPath.equals("/") ? "/" + name : parentPath + "/" + name;
    }

    public abstract long sizeBytes();
}
