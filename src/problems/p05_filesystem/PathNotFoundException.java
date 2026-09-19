package problems.p05_filesystem;

public class PathNotFoundException extends RuntimeException {
    public PathNotFoundException(String path) {
        super("no such path: " + path);
    }
}
