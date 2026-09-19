package patterns.structural.composite;

/** Leaf: carries its own size, no children. */
public class File implements FsNode {
    private final String name;
    private final long sizeBytes;

    public File(String name, long sizeBytes) {
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("size cannot be negative: " + sizeBytes);
        }
        this.name = name;
        this.sizeBytes = sizeBytes;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long sizeBytes() {
        return sizeBytes;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + name + " (" + sizeBytes + " B)");
    }
}
