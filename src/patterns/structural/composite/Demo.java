package patterns.structural.composite;

public class Demo {
    public static void main(String[] args) {
        Directory root = new Directory("root");
        Directory docs = new Directory("docs");
        Directory notes = new Directory("notes");

        root.add(new File("a.txt", 100)).add(docs);
        docs.add(new File("b.txt", 250)).add(notes);
        notes.add(new File("c.txt", 50));

        root.print("");

        // One node or a whole subtree — identical handling for the caller.
        FsNode[] nodes = {new File("a.txt", 100), docs};
        for (FsNode node : nodes) {
            System.out.println("size of " + node.name() + " = " + node.sizeBytes() + " B");
        }

        // Failure path: duplicate names are impossible, not silently overwritten.
        try {
            docs.add(new File("b.txt", 999));
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
