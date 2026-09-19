package problems.p01_connectfour;

/**
 * Rules behind an interface — swapping to Tic-Tac-Toe or Gomoku is a new
 * rule (or just new config), never an edit to Game or Board.
 */
public interface WinRule {
    boolean isWin(Board board, int lastRow, int lastCol);
}
