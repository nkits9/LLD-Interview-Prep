package problems.p05_filesystem;

/** Leaf. */
public class File extends FsNode {
    private final long sizeBytes;

    File(String name, long sizeBytes) {
        super(name);
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("size cannot be negative");
        }
        this.sizeBytes = sizeBytes;
    }

    @Override
    public long sizeBytes() {
        return sizeBytes;
    }
}
