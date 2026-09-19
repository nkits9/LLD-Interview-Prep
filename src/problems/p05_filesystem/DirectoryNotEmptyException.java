package problems.p05_filesystem;

public class DirectoryNotEmptyException extends RuntimeException {
    public DirectoryNotEmptyException(String path) {
        super("directory not empty: " + path);
    }
}
