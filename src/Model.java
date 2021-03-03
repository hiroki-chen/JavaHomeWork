
public class Model {
    private static final int WIDTH = 19;
    private Color lastColor;
    private Color chessBoard[][] = new Color[WIDTH][WIDTH];
    private static Model instance;
    private static boolean win;

    private Model() {
        for (int i = 0; i < Model.WIDTH; i++) {
            for (int j = 0; j < Model.WIDTH; j++) {
                chessBoard[i][j] = Color.NONE;
            }
        }
        lastColor = Color.WHITE;
    }

    private boolean check(int row, int col) {
        return !(row >= 19 || col >= 19 || row < 0 || col < 0);
    }

    private boolean checkWin(int i, int j) {
        int cnt = 1;
        for (int row = i + 1; row < 19; row ++) {
            if (chessBoard[row][j] == Color.NONE) { break; }
            if (chessBoard[row][j] == chessBoard[row - 1][j]) { cnt += 1; }
            if (cnt == 5) { win = true; return true; }
        }

        for (int row = i - 1; row >= 0; row --) {
            if (chessBoard[row][j] == Color.NONE) { break; }
            if (chessBoard[row][j] == chessBoard[row + 1][j]) { cnt += 1; }
            if (cnt == 5) { win = true; return true; }
        }
        cnt = 1;
        for (int col = j + 1; col < 19; col ++) {
            if (chessBoard[i][col] == Color.NONE) { break; }
            if (chessBoard[i][col] == chessBoard[i][col - 1]) { cnt += 1;}
            if (cnt == 5) { win = true; return true; }
        }

        for (int col = j - 1; col > 0; col --) {
            if (chessBoard[i][col] == Color.NONE) { break; }
            if (chessBoard[i][col] == chessBoard[i][col + 1]) { cnt += 1;}
            if (cnt == 5) { win = true; return true; }
        }

        int row = i + 1, col = j + 1;
        cnt = 1;
        while (row < 19 && col < 19) {
            if (chessBoard[row][col] == Color.NONE) { break; }
            if (chessBoard[row - 1][col - 1] == chessBoard[row][col]) { cnt += 1;}
            row += 1;
            col += 1;
            if (cnt == 5) { win = true; return true; }
        }

        row = i - 1; col = j - 1;
        while (row >= 0 && col >= 0) {
            if (chessBoard[row][col] == Color.NONE) { break; }
            if (chessBoard[row + 1][col + 1] == chessBoard[row][col]) { cnt += 1;}
            row -= 1;
            col -= 1;
            if (cnt == 5) { win = true; return true; }
        }

        row = i + 1; col = j - 1; cnt = 1;
        while (row < 19 && col >= 0) {
            if (chessBoard[row][col] == Color.NONE) { break; }
            if (chessBoard[row - 1][col + 1] == chessBoard[row][col]) { cnt += 1;}
            row += 1;
            col -= 1;
            if (cnt == 5) { win = true; return true; }
        }
        
        row = i - 1; col = j + 1;
        while (row >= 0 && col < 19) {
            if (chessBoard[row][col] == Color.NONE) { break; }
            if (chessBoard[row + 1][col - 1] == chessBoard[row][col]) { cnt += 1;}
            row -= 1;
            col += 1;
            if (cnt == 5) { win = true; return true; }
        }

        return false;
    }

    public boolean isWin() {
        return win;
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public PutChessState putChess(SendPackage pkg) {
        int row = pkg.getRow();
        int col = pkg.getCol();
        
        if (!check(row, col)) {
            return PutChessState.WRONG_POSITION;
        } else if (pkg.getColor() == lastColor) {
            return PutChessState.TWICE;
        } else {
            chessBoard[row][col] = pkg.getColor();
            lastColor = pkg.getColor();

            if (checkWin(row, col)) {
                return PutChessState.WIN;
            }
            return PutChessState.NORMAL;
        }
    }

    public Color getChess(int row, int col) {
        return check(row, col) ? chessBoard[row][col] : Color.NONE;
    }

    public Color[][] getChessBoard() {
        return chessBoard;
    }

    public Color getRound() {
        return lastColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public void clear() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                chessBoard[i][j] = Color.NONE;
            }
        }
    }

    public void print() {
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (chessBoard[i][j] == Color.NONE) {
                    System.out.print(". ");
                } else {
                    System.out.print((chessBoard[i][j] == Color.BLACK ? "B " : "W "));
                }
            }
            System.out.print("\n");
        }
    }

    public void setToNone(int row, int col) {
        if(check(row, col)) {
            chessBoard[row][col] = Color.NONE;
        }
    }

    public void setLastColor(Color lastColor) {
        this.lastColor = lastColor;
    }

    public void reverseRound() {
        lastColor = lastColor == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
}
