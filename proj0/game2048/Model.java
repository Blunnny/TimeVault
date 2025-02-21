package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author TODO: YOUR NAME HERE
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed = false;
        board.setViewingPerspective(side);
        int size = board.size();
        // 记录每个位置是否已发生过合并，防止一次操作中同一棋子重复合并
        boolean[][] merged = new boolean[size][size];

        // 注意：board.tile(col, row)中 row = size-1 为最顶部
        // 从倒数第二行开始向下遍历，每个列独立处理
        for (int row = size - 2; row >= 0; row--) {
            for (int col = 0; col < size; col++) {
                Tile t = board.tile(col, row);
                if (t == null) continue; // 无棋子则跳过

                int targetRow = row; // 初始目标位置为原位置
                // 向上查找能移动到的最远位置
                for (int r = row + 1; r < size; r++) {
                    Tile next = board.tile(col, r);
                    if (next == null) {
                        // 当前位置为空，棋子可进一步上移
                        targetRow = r;
                    } else if (next.value() == t.value() && !merged[r][col]) {
                        // 找到相同且未合并的棋子，可合并：目标位置即为 r
                        targetRow = r;
                        break;
                    } else {
                        // 遇到不同或者已经合并的棋子，不能再上移：停在上一个空位
                        break;
                    }
                }

                // 如果目标位置与原位置不同，则需要移动棋子
                if (targetRow != row) {
                    Tile dest = board.tile(col, targetRow);
                    if (dest != null && dest.value() == t.value() && !merged[targetRow][col]) {
                        // 目标位置有相同值且未合并，执行合并操作
                        if (board.move(col, targetRow, t)) {
                            // board.move 返回 true 表示发生了合并，更新分数
                            score += board.tile(col, targetRow).value();
                            merged[targetRow][col] = true;
                        }
                    } else {
                        // 目标位置为空，只需移动棋子到目标位置
                        board.move(col, targetRow, t);
                    }
                    changed = true;
                }
            }
        }

        board.setViewingPerspective(Side.NORTH);
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }


    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        int size = b.size();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (b.tile(col, row) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        int size = b.size();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Tile currentTile = b.tile(col, row);
                if (currentTile != null && currentTile.value() == Model.MAX_PIECE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        if (emptySpaceExists(b)) {
            return true;
        }

        int size = b.size();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Tile currentTile = b.tile(col, row);
                /* 检查列 (上下） */
                if (row + 1 < size) {
                    Tile downTile = b.tile(col, row + 1);
                    if (currentTile.value() == downTile.value()) {
                        return true;
                    }
                }
                /* 检查行 (左右） */
                if (col + 1 < size) {
                    Tile rightTile = b.tile(col + 1, row);
                    if (currentTile.value() == rightTile.value()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
