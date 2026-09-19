package problems.extras.snakeladder;

import java.util.Map;

/** Squares 1..size with jumps (snakes go down, ladders go up) — validated so chains can't exist. */
public class Board {
    private final int size;
    private final Map<Integer, Integer> jumps;

    public Board(int size, Map<Integer, Integer> jumps) {
        if (size < 10) {
            throw new IllegalArgumentException("board too small: " + size);
        }
        for (Map.Entry<Integer, Integer> jump : jumps.entrySet()) {
            int from = jump.getKey();
            int to = jump.getValue();
            if (from <= 1 || from >= size || to < 1 || to >= size || from == to) {
                throw new IllegalArgumentException("invalid jump " + from + "->" + to);
            }
            if (jumps.containsKey(to)) {
                throw new IllegalArgumentException(
                        "jump chain at " + to + " — a jump may not land on another jump");
            }
        }
        this.size = size;
        this.jumps = Map.copyOf(jumps);
    }

    public int size() {
        return size;
    }

    /** Where you actually end up after landing on a square. */
    public int resolve(int square) {
        return jumps.getOrDefault(square, square);
    }

    public boolean isSnake(int square) {
        return jumps.containsKey(square) && jumps.get(square) < square;
    }

    public boolean isLadder(int square) {
        return jumps.containsKey(square) && jumps.get(square) > square;
    }
}
