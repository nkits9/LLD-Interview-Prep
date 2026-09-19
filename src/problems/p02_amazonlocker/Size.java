package problems.p02_amazonlocker;

/** Ordered enum — compatibility is a method, never an if-else chain at call sites. */
public enum Size {
    SMALL, MEDIUM, LARGE;

    /** A locker fits any package of its size or smaller. */
    public boolean fits(Size packageSize) {
        return this.ordinal() >= packageSize.ordinal();
    }
}
