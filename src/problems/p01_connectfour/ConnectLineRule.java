package problems.p01_connectfour;

/** N-in-a-line, any direction. Win length is config: 4 = Connect Four, 5 = Gomoku. */
public class ConnectLineRule implements WinRule {
    private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}}; // — | / \

    private final int winLength;

    public ConnectLineRule(int winLength) {
        if (winLength < 2) {
            throw new IllegalArgumentException("win length must be >= 2, got " + winLength);
        }
        this.winLength = winLength;
    }

    /**
     * Only lines THROUGH the last move can newly win — so check the 4 lines
     * around it, O(winLength), never a full-board scan.
     */
    @Override
    public boolean isWin(Board board, int lastRow, int lastCol) {
        Piece piece = board.pieceAt(lastRow, lastCol);
        if (piece == null) {
            return false;
        }
        for (int[] d : DIRECTIONS) {
            int lineLength = 1
                    + countSame(board, piece, lastRow, lastCol, d[0], d[1])
                    + countSame(board, piece, lastRow, lastCol, -d[0], -d[1]);
            if (lineLength >= winLength) {
                return true;
            }
        }
        return false;
    }

    private int countSame(Board board, Piece piece, int row, int col, int dRow, int dCol) {
        int count = 0;
        int r = row + dRow;
        int c = col + dCol;
        while (board.pieceAt(r, c) == piece) { // pieceAt returns null out of bounds
            count++;
            r += dRow;
            c += dCol;
        }
        return count;
    }
}
