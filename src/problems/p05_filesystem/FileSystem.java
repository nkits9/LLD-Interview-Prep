package problems.p05_filesystem;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/** In-memory file system: path resolution, CRUD, move with cycle prevention. */
public class FileSystem {
    private final Directory root = new Directory("");

    /** Normalizes '.', '..', repeated and trailing slashes — parsing lives in ONE place. */
    private FsNode resolve(String path) {
        if (path == null || !path.startsWith("/")) {
            throw new PathNotFoundException(path + " (paths are absolute)");
        }
        Deque<String> parts = new ArrayDeque<>();
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) {
                continue; // repeated or trailing slash, current dir
            }
            if (part.equals("..")) {
                parts.pollLast(); // above root stays at root
            } else {
                parts.addLast(part);
            }
        }
        FsNode current = root;
        for (String part : parts) {
            if (!(current instanceof Directory)) {
                throw new PathNotFoundException(path);
            }
            current = ((Directory) current).child(part);
            if (current == null) {
                throw new PathNotFoundException(path);
            }
        }
        return current;
    }

    private Directory resolveDir(String path) {
        FsNode node = resolve(path);
        if (!(node instanceof Directory)) {
            throw new PathNotFoundException(path + " is not a directory");
        }
        return (Directory) node;
    }

    public void mkdir(String parentPath, String name) {
        resolveDir(parentPath).addChild(new Directory(name));
    }

    public void createFile(String parentPath, String name, long sizeBytes) {
        resolveDir(parentPath).addChild(new File(name, sizeBytes));
    }

    public List<String> ls(String path) {
        return resolveDir(path).list();
    }

    public long sizeOf(String path) {
        return resolve(path).sizeBytes();
    }

    /** Files always; directories only when empty — no silent recursive nukes. */
    public void delete(String path) {
        FsNode node = resolve(path);
        if (node.parent() == null) {
            throw new IllegalMoveException("cannot delete root");
        }
        if (node instanceof Directory && !((Directory) node).isEmpty()) {
            throw new DirectoryNotEmptyException(path);
        }
        node.parent().removeChild(node);
    }

    /**
     * Move/rename. Two write locks are taken in CANONICAL (path) order — two
     * concurrent moves between the same pair of directories cannot deadlock.
     * Cycle prevention: the destination must not be the moved directory or
     * any of its descendants (walk destination's ancestors).
     */
    public void move(String srcPath, String destDirPath, String newName) {
        FsNode node = resolve(srcPath);
        Directory destination = resolveDir(destDirPath);
        Directory source = node.parent();
        if (source == null) {
            throw new IllegalMoveException("cannot move root");
        }
        for (FsNode ancestor = destination; ancestor != null; ancestor = ancestor.parent()) {
            if (ancestor == node) {
                throw new IllegalMoveException(
                        "cannot move " + srcPath + " into its own subtree " + destDirPath);
            }
        }
        Directory first = source.absolutePath().compareTo(destination.absolutePath()) <= 0
                ? source : destination;
        Directory second = first == source ? destination : source;
        first.lock().writeLock().lock();
        second.lock().writeLock().lock();
        try {
            String originalName = node.name();
            source.removeChild(node);
            node.rename(newName);
            try {
                destination.addChild(node);
            } catch (NameConflictException e) {
                node.rename(originalName); // roll back — a failed move leaves the tree unchanged
                source.addChild(node);
                throw e;
            }
        } finally {
            second.lock().writeLock().unlock();
            first.lock().writeLock().unlock();
        }
    }
}
