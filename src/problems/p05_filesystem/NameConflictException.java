package problems.p05_filesystem;

public class NameConflictException extends RuntimeException {
    public NameConflictException(String dirPath, String name) {
        super("'" + name + "' already exists in " + dirPath);
    }
}
