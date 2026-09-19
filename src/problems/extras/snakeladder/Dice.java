package problems.extras.snakeladder;

import java.util.Random;

/** Randomness is injected (seeded Random) — games are replayable in tests. */
public class Dice {
    private final Random random;
    private final int faces;

    public Dice(Random random, int faces) {
        this.random = random;
        this.faces = faces;
    }

    public int roll() {
        return random.nextInt(faces) + 1;
    }
}
