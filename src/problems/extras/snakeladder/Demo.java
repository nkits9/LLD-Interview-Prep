package problems.extras.snakeladder;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class Demo {
    public static void main(String[] args) {
        // Board validation: chained jumps are unrepresentable.
        try {
            new Board(30, Map.of(3, 12, 12, 20));
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        Board board = new Board(30, Map.of(
                3, 16,    // ladder
                7, 21,    // ladder
                27, 4,    // snake
                18, 11)); // snake
        Game game = new Game(board, new Dice(new Random(7), 6),
                OvershootPolicy.STAY, List.of("asha", "bala"));

        int turns = 0;
        while (game.winner() == null && turns++ < 200) {
            System.out.println(game.playTurn());
        }
        System.out.println("winner: " + game.winner() + " in " + turns + " turns");

        // Game over: further turns rejected explicitly.
        try {
            game.playTurn();
        } catch (IllegalStateException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
