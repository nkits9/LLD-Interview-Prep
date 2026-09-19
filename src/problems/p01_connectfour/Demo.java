package problems.p01_connectfour;

public class Demo {
    public static void main(String[] args) {
        Player red = new Player("Ankit", Piece.RED);
        Player yellow = new Player("Bot", Piece.YELLOW);

        // Configurable size and win length: a 4x5 "Connect 3".
        Game game = new Game(new Board(4, 5), new ConnectLineRule(3), red, yellow);

        // Failure path 1: out-of-bounds column — rejected, turn unchanged.
        try {
            game.play(9);
        } catch (InvalidMoveException e) {
            System.out.println("rejected : " + e.getMessage());
        }

        for (int col : new int[]{0, 1, 0, 1, 0}) { // RED stacks column 0 → vertical win
            System.out.println(game.currentPlayer().name() + " plays column " + col);
            game.play(col);
        }
        System.out.print(game.board().render());
        System.out.println("status   : " + game.status()
                + ", winner: " + game.winner().map(Player::name).orElse("-"));

        // Failure path 2: no moves after the game ends.
        try {
            game.play(2);
        } catch (GameOverException e) {
            System.out.println("rejected : " + e.getMessage());
        }

        // Draw detection + full column, on a 2x3 board where 3-in-a-line is impossible.
        System.out.println("\n-- tiny board: full column and draw --");
        Game tiny = new Game(new Board(2, 3), new ConnectLineRule(3), red, yellow);
        tiny.play(0); // R bottom of col 0
        tiny.play(1); // Y
        tiny.play(2); // R
        tiny.play(0); // Y tops off col 0
        try {
            tiny.play(0); // Failure path 3: column full
        } catch (InvalidMoveException e) {
            System.out.println("rejected : " + e.getMessage());
        }
        tiny.play(1); // R (same player retries a valid column — turn survived the rejection)
        tiny.play(2); // Y → board full, nobody won
        System.out.print(tiny.board().render());
        System.out.println("status   : " + tiny.status());
    }
}
