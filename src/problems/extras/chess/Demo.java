package problems.extras.chess;

public class Demo {
    public static void main(String[] args) {
        ChessGame game = new ChessGame();

        game.move("e2", "e4");                     // white pawn double push
        game.move("e7", "e5");                     // black answers
        game.move("d1", "h5");                     // white queen out the cleared diagonal
        game.move("b8", "c6");                     // black knight develops
        System.out.println("position ok: e4=" + game.pieceAt("e4") + " h5=" + game.pieceAt("h5")
                + " c6=" + game.pieceAt("c6"));

        // Failure paths: wrong turn, blocked path, bad geometry, own piece.
        try {
            game.move("c6", "d4");                 // it's WHITE's turn
        } catch (IllegalMoveException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            game.move("a1", "a3");                 // rook blocked by own a2 pawn
        } catch (IllegalMoveException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            game.move("h5", "h2");                 // queen path is clear but h2 is white's own pawn
        } catch (IllegalMoveException e) {
            System.out.println("rejected: " + e.getMessage());
        }

        // Capture + CHECK detection.
        boolean check = game.move("h5", "e5");     // queen takes the e5 pawn along rank 5
        System.out.println("Qxe5 captured pawn, check on black: " + check);

        // King safety: a move that ignores the check is rolled back automatically.
        try {
            game.move("a7", "a6");
        } catch (IllegalMoveException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        // Blocking the check IS legal.
        game.move("g8", "e7");                     // knight blocks on e7
        System.out.println("block ok : e7=" + game.pieceAt("e7"));

        // Command undo restores the capture exactly.
        game.undo();                               // un-block
        game.undo();                               // un-capture: pawn back on e5, queen back on h5
        System.out.println("2 undos  : e5=" + game.pieceAt("e5") + " h5=" + game.pieceAt("h5"));
    }
}
