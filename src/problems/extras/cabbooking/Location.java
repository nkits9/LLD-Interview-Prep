package problems.extras.cabbooking;

/** Grid coordinates; Manhattan distance stands in for road distance. */
public final class Location {
    private final int x;
    private final int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int distanceTo(Location other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
