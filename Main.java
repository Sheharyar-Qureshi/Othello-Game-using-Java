package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Scanner;
import java.util.Vector;

public class Main {
    public static JFrame jFrame = new JFrame("GAME");
    public static Board board = new Board();

    public static void main(String[] args) {
        int depth;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter The Depth");
        while (true){
            try{
                depth = scanner.nextInt();
                if(depth>0 && depth<=7){
                    System.out.println("The Depth IS Set : "+depth);
                    break;
                }else{
                    throw new Exception("The Input is invalid");
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage());
            }
        }

        GameEngine.maxDepth = depth;
        MouseClick mouseClick = new MouseClick();
        board.addMouseListener(mouseClick);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setSize(610, 850);
        jFrame.setResizable(false);
        jFrame.add(board);
        jFrame.setVisible(true);
    }
}


class Board extends JPanel {
    private static int blockSize = 50;
    private static int blockDistance = 10;
    Vector<int[]> guide;
    static boolean time = true;
    static int[] computerMove;
    static boolean gameOver = false;
    static boolean invalidMove = false;

    public void addGuide(Vector<int[]> guide) {
        this.guide = guide;
    }

    public void paintComponent(Graphics g) {
        if (time) {
            Main.board.addGuide(GameEngine.instance.showMovesPlayer());
            time = false;
        }
        super.paintComponent(g);
        this.setBackground(Color.lightGray);

        for (int x = 1; x < 9; x++) {
            for (int y = 1; y < 9; y++) {
                if (GameEngine.board[y - 1][x - 1] == GameEngine.instance.PLAYER) {
                    g.setColor(Color.white);
                    g.fillOval((x * blockSize) + (x * blockDistance), (y * blockSize) + (y * blockDistance), blockSize, blockSize);
                } else if (GameEngine.board[y - 1][x - 1] == GameEngine.instance.COMPUTER) {
                    g.setColor(Color.black);
                    g.fillOval((x * blockSize) + (x * blockDistance), (y * blockSize) + (y * blockDistance), blockSize, blockSize);
                } else {
                    g.setColor(Color.gray);
                    g.fillOval((x * blockSize) + (x * blockDistance), (y * blockSize) + (y * blockDistance), blockSize, blockSize);
                }
            }
        }
        if (guide != null)
            for (int[] ints : guide) {
                int y = ints[0] + 1;
                int x = ints[1] + 1;
                g.setColor(Color.MAGENTA);
                g.drawOval((x * blockSize) + (x * blockDistance), (y * blockSize) + (y * blockDistance), blockSize, blockSize);
            }
        if (computerMove != null) {
            int y = computerMove[0] + 1;
            int x = computerMove[1] + 1;
            g.setColor(Color.RED);
            g.drawOval((x * blockSize) + (x * blockDistance), (y * blockSize) + (y * blockDistance), blockSize, blockSize);
        }
        int[] score = GameEngine.instance.CountScore();
        g.setColor(Color.black);
        g.setFont(new Font("Courier", Font.BOLD, 20));
        g.drawString("Player Score   : " + score[1], 30, 575);
        g.drawString("Computer Score : " + score[0], 30, 600);
        if (gameOver) {
            g.setFont(new Font("Courier", Font.BOLD, 30));
            if (score[0] > score[1]) {
                g.setColor(Color.RED);
                g.drawString("Computer WINS", 30, 650);

            } else if (score[0] < score[1]) {
                g.setColor(Color.GREEN);
                g.drawString("YOU WIN  ", 30, 650);

            } else {
                g.setColor(Color.YELLOW);
                g.drawString("LOL ITS DRAW  ", 30, 650);
            }
        } else {
            if (invalidMove) {
                g.setColor(Color.red);
                g.drawString("INVALID MOVE", 30, 650);
            } else {
                g.setColor(Color.orange);
                g.drawString("ON GOING...  ", 30, 650);
            }
        }
    }

    static int translateX(int qX) {
        int x = (qX + blockDistance - ((qX / (blockSize + blockDistance)))) / (blockSize + blockDistance);
        return x;
    }

    static int translateY(int qY) {
        int y = (qY + blockDistance - ((qY / (blockSize + blockDistance)))) / (blockSize + blockDistance);
        return y;
    }

}

class MouseClick implements MouseListener {

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        int c = Board.translateX(mouseEvent.getX()) - 1;
        int r = Board.translateY(mouseEvent.getY()) - 1;
        System.out.println("-------------------------------------------------------");
        if (0 <= r && r < 8 && 0 <= c && c < 8) {
            if (GameEngine.instance.CheckForEndGameMoves('p')) {
                System.out.println("END GAME BY PLAYER");
                Board.gameOver = true;
            } else {
                Board.invalidMove = false;
                if (GameEngine.instance.IsValidMovePlayer(r, c)) {
                    GameEngine.instance.TakeTurnPlayer(r, c);
                    GameEngine.instance.slots--;
                    Main.jFrame.revalidate();
                    Main.jFrame.repaint();
                    if (GameEngine.instance.CheckForEndGameMoves('c')) {
                        System.out.println("END GAME BY COMPUTER");
                        Board.gameOver = true;
                    } else {
                        GameEngine.totalTress = 0;
                        GameEngine.instance.ComputerMove();
                        System.out.println("TOTAL TREES -- > " + GameEngine.totalTress);
                        Board.time = true;
                        GameEngine.instance.slots--;
                        if (GameEngine.instance.CheckForEndGameMoves('p')) {
                            System.out.println("END GAME BY PLAYER");
                            Board.gameOver = true;
                        }
                    }
                } else {
                    System.out.println("INVALID MOVE");
                    Board.invalidMove = true;
                }
            }
        } else {
            System.out.println("Invalid Move");
        }
        Main.jFrame.revalidate();
        Main.jFrame.repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}

class GameEngine {
    public static long totalTress = 0;
    public static char[][] board;
    public static int maxDepth = 5;
    //    public char turn;
    int slots = 60;
    public static GameEngine instance = new GameEngine();
    int MAX = 1000;
    int MIN = -1000;
    final char PLAYER = 'p';
    final char COMPUTER = 'c';
    final char BLANK = '.';

    private GameEngine() {
        board = new char[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                board[i][j] = '.';
        board[3][3] = 'c';
        board[3][4] = 'p';
        board[4][3] = 'p';
        board[4][4] = 'c';
    }

    int cel(int x, int y) {
        if (x >= 0 && x <= 7 && y <= 7 && y >= 0) {
            return board[x][y];
        } else
            return -1;
    }

    public boolean IsValidMovePlayer(int r, int c) {
        if (board[r][c] != '.')
            return false;
        return IsValidMove(PLAYER, r, c, board);
    }

    public boolean IsValidMove(char turn, int r, int c, char[][] board) {
        boolean total = false;
        char opp = PLAYER;
        if (turn == PLAYER) {
            opp = COMPUTER;
        }
        if (r + 1 <= 7) // Check Down
        {
            int turned = 0;
            if (cel(r + 1, c) == opp) {
                int checkx = r;
                while (cel(++checkx, c) == opp) {
                }
                if (cel(checkx, c) == turn) {
                    int temp = checkx;
                    while (--checkx > r) {
                        turned++;
                    }
                }
                if (turned > 0)
                    return true;
            }
        }
        if (r - 1 >= 0) {   //Check UP
            int turned = 0;
            if (cel(r - 1, c) == opp) {
                int checkx = r;
                while (cel(--checkx, c) == opp) {
                }
                int temp = checkx;
                if (cel(checkx, c) == turn) {
                    while (++checkx < r) {
                        //board[checkx][y] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c - 1 >= 0) {   //Check Left
            int turned = 0;
            if (cel(r, c - 1) == opp) {
                int checky = c;
                while (cel(r, --checky) == opp) {
                }
                int temp = checky;
                if (cel(r, checky) == turn) {
                    while (++checky < c) {
                        //board[x][checky] = mine;
                        turned++;
                    }

                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c + 1 <= 7) {   //Check Right
            int turned = 0;
            if (cel(r, c + 1) == opp) {
                int checky = c;
                while (cel(r, ++checky) == opp) {
                }
                if (cel(r, checky) == turn) {
                    int temp = checky;
                    while (--checky > c) {
                        //board[x][checky] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c + 1 <= 7 && r + 1 <= 7) {
            int turned = 0;
            if (cel(r + 1, c + 1) == opp) {
                int checky = c;
                int checkx = r;
                while (cel(++checkx, ++checky) == opp) {
                }

                if (cel(checkx, checky) == turn) {
                    int tx = checkx;
                    int ty = checky;
                    while (--checky > c && --checkx > r) {
                        //board[checkx][checky] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c - 1 <= 7 && r - 1 <= 7) {
            int turned = 0;
            if (cel(r - 1, c - 1) == opp) {
                int checky = c;
                int checkx = r;
                while (cel(--checkx, --checky) == opp) {
                }
                if (cel(checkx, checky) == turn) {
                    int tx = checkx;
                    int ty = checky;
                    while (++checky < c && ++checkx < r) {
                        //board[checkx][checky] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c - 1 >= 0 && r + 1 <= 7) {
            int turned = 0;
            if (cel(r + 1, c - 1) == opp) {
                int checky = c;
                int checkx = r;
                while (cel(++checkx, --checky) == opp) {
                }
                int tx = checkx;
                int ty = checky;
                if (cel(checkx, checky) == turn) {
                    while (++checky < c && --checkx > r) {
                        //board[checkx][checky] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        if (c + 1 <= 7 && r - 1 >= 0) {
            int turned = 0;
            if (cel(r - 1, c + 1) == opp) {
                int checky = c;
                int checkx = r;
                while (cel(--checkx, ++checky) == opp) {
                }
                if (cel(checkx, checky) == turn) {
                    int tx = checkx;
                    int ty = checky;
                    while (--checky > c && ++checkx < r) {
//                        board[checkx][checky] = mine;
                        turned++;
                    }
                    if (turned > 0)
                        return true;
                }
            }
        }
        return total;
    }


    void TakeTurnPlayer(int r, int c) {
        takeTurn(PLAYER, r, c, board);
    }

    void takeTurn(char turn, int row, int col, char[][] board) {
        board[row][col] = turn;
        kernalTestFromPoint(row, col, turn, 0, 1, board);
        kernalTestFromPoint(row, col, turn, 0, -1, board);
        kernalTestFromPoint(row, col, turn, 1, 0, board);
        kernalTestFromPoint(row, col, turn, -1, 0, board);
        kernalTestFromPoint(row, col, turn, 1, 1, board);
        kernalTestFromPoint(row, col, turn, 1, -1, board);
        kernalTestFromPoint(row, col, turn, -1, 1, board);
        kernalTestFromPoint(row, col, turn, -1, -1, board);
    }

    private void kernalTestFromPoint(int row, int column, char colour, int colDir, int rowDir, char[][] grid) {
        int currentRow = row + rowDir;
        int currentCol = column + colDir;
        if (currentRow == 8 || currentRow < 0 || currentCol == 8 || currentCol < 0) {
            return;
        }
        while (grid[currentRow][currentCol] == COMPUTER || grid[currentRow][currentCol] == PLAYER) {
            if (grid[currentRow][currentCol] == colour) {
                while (!(row == currentRow && column == currentCol)) {
                    grid[currentRow][currentCol] = colour;
                    currentRow = currentRow - rowDir;
                    currentCol = currentCol - colDir;
                }
                break;
            } else {
                currentRow = currentRow + rowDir;
                currentCol = currentCol + colDir;
            }
            if (currentRow < 0 || currentCol < 0 || currentRow == 8 || currentCol == 8) {
                break;
            }
        }
    }

    Vector<int[]> allPossibleMoves(char[][] board, char turn) {
        Vector<int[]> allMoves = new Vector<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == '.') {
                    if (IsValidMove(turn, r, c, board)) {
                        int[] arr = new int[2];
                        arr[0] = r;
                        arr[1] = c;
                        allMoves.add(arr);
                        totalTress++;
                    }
                }
            }
        }
        return allMoves;
    }

    void printAllMoves(Vector<int[]> arr) {
        for (int[] ints : arr) {
            System.out.printf("[%d %d]", ints[0], ints[1]);
        }
        System.out.println();
    }

    char[][] copyBoard(char[][] board) {
        char[][] newBoard = new char[8][8];
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                newBoard[x][y] = board[x][y];
            }
        return newBoard;
    }

    Vector<int[]> showMovesPlayer() {
        return showMoves(board, PLAYER);
    }

    Vector<int[]> showMoves(char[][] board, char turn) {
        Vector<int[]> vector = allPossibleMoves(board, turn);
        printAllMoves(vector);
        return vector;
    }

    void printBoard(char[][] board) {
        System.out.println("_______________");
        System.out.println("1 2 3 4 5 6 7 8");
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }

    int eFun(char[][] board) {
        int computer = 0;
        int player = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] == 'c')
                    computer++;
                if (board[r][c] == 'p')
                    player++;
            }
        }
        return computer - player;
    }

    Vector<char[][]> makeChildren(char[][] board, char mine) {
        Vector<char[][]> children = new Vector<>();
        Vector<int[]> combination;
        char[][] newBoard;
        combination = allPossibleMoves(board, mine);
        for (int[] ints : combination) {
            newBoard = copyBoard(board);
            takeTurn(mine, ints[0], ints[1], newBoard);
            children.add(newBoard);
        }
        return children;
    }

    void ComputerMove() {
        board = computerBestMove(board);
    }

    char[][] computerBestMove(char[][] board) {
        int best = -1000;
        char[][] bestMoveBoard = copyBoard(board);
        Integer beta = MAX;
        Integer alpha = MIN;
        Integer depth = 1;
        int oo = 0;
        int moveID = 0;
        Vector<int[]> vector2 = allPossibleMoves(board, COMPUTER);
        Vector<char[][]> vector = makeChildren(board, COMPUTER);
        for (char[][] chars : vector) {
            int v = alphaBeta(depth, false, chars, alpha, beta);
            if (best <= v) {
                moveID = oo;
                best = alpha;                                                                                           // SUSPICIOUS??//
                bestMoveBoard = copyBoard(chars);
            }
            oo++;
        }
        Board.computerMove = vector2.get(moveID);
        return bestMoveBoard; 
    }

    int alphaBeta(Integer depth, Boolean maximizingPlayer, char[][] board, Integer alpha, Integer beta) {
        if (depth == maxDepth) {
            return eFun(board);
        }
        if (maximizingPlayer) {
            int best = MAX;
            Vector<char[][]> children = makeChildren(board, COMPUTER);
            for (char[][] child : children) {
                int v = alphaBeta(depth + 1, true, child, alpha, beta);
                best = Math.max(v, best);
                alpha = Math.max(alpha, best);
                if (beta <= alpha)
                    break;
            }
            return best;
        } else {
            int best = MIN;
            Vector<char[][]> children = makeChildren(board, PLAYER);
            for (char[][] child : children) {
                int v = alphaBeta(depth + 1, false, child, alpha, beta);
                best = Math.min(beta, v);
                beta = Math.min(beta, best);
                if (beta <= alpha)
                    break;
            }
            return best;
        }
    }

    boolean CheckForEndGameMoves(char turn) {
        if (turn == PLAYER) {
            Vector<int[]> vectorP = allPossibleMoves(board, PLAYER);
            System.out.print("PLAYER : ");
            printAllMoves(vectorP);
            return vectorP.size() == 0;
        } else {
            Vector<int[]> vectorC = allPossibleMoves(board, COMPUTER);
            System.out.print("COMPUTER : ");
            printAllMoves(vectorC);
            return vectorC.size() == 0;
        }
    }

    int[] CountScore() {
        int[] score = new int[2];
        score[0] = 0;
        score[1] = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == PLAYER)
                    score[1]++;
                if (board[i][j] == COMPUTER)
                    score[0]++;
            }
        }
        return score;
    }
}
