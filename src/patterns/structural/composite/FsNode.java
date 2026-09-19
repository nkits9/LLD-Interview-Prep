package patterns.structural.composite;

/** Uniform node: callers treat one file and a whole directory tree identically. */
public interface FsNode {
    String name();

    long sizeBytes();

    void print(String indent);
}
