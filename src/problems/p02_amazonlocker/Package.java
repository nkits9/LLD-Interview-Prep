package problems.p02_amazonlocker;

public final class Package {
    private final String id;
    private final Size size;

    public Package(String id, Size size) {
        this.id = id;
        this.size = size;
    }

    public String id() {
        return id;
    }

    public Size size() {
        return size;
    }

    @Override
    public String toString() {
        return id + "(" + size + ")";
    }
}
