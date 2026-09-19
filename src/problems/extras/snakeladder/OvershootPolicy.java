package problems.extras.snakeladder;

/** The house rule that varies — an enum-sized Strategy. */
public enum OvershootPolicy {
    /** Need the exact roll to finish; overshoot wastes the turn. */
    STAY,
    /** Overshoot bounces back from the last square. */
    BOUNCE;

    int apply(int target, int boardSize) {
        if (target <= boardSize) {
            return target;
        }
        return this == STAY ? -1 : 2 * boardSize - target; // -1 = don't move
    }
}
