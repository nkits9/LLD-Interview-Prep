package problems.p01_connectfour;

/**
 * Pure state: knows how pieces stack, nothing about turns or winning.
 * Row 0 is the bottom. Size is configurable — nothing hardcodes 6x7.
 */
public class Board {
    private final int rows;
    private final int cols;
    private final Piece[][] grid; // [row][col]
    private int piecesPlaced;

    public Board(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("board must be at least 1x1, got " + rows + "x" + cols);
        }
        this.rows = rows;
        this.cols = cols;
        this.grid = new Piece[rows][cols];
    }

    /**
     * Gravity drop. Validates BEFORE mutating — a rejected move leaves the
     * board (and the caller's turn order) untouched.
     *
     * @return the row the piece landed in — the win check needs it
     */
    public int drop(int col, Piece piece) {
        if (col < 0 || col >= cols) {
            throw new InvalidMoveException("column " + col + " is out of bounds [0.." + (cols - 1) + "]");
        }
        for (int row = 0; row < rows; row++) {
            if (grid[row][col] == null) {
                grid[row][col] = piece;
                piecesPlaced++;
                return row;
            }
        }
        throw new InvalidMoveException("column " + col + " is full");
    }

    /** Null for empty cells AND out-of-bounds — keeps win-rule scans clean. */
    public Piece pieceAt(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return null;
        }
        return grid[row][col];
    }

    public boolean isFull() {
        return piecesPlaced == rows * cols;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int row = rows - 1; row >= 0; row--) {
            for (int col = 0; col < cols; col++) {
                sb.append(grid[row][col] == null ? '.' : grid[row][col].symbol()).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
