package problems.p09_inventory;

/** Returns and damaged goods are their OWN movement types — never negative restocks. */
public enum MovementType {
    RESTOCK, RESERVE, RELEASE, COMMIT, RETURN, DAMAGED
}
