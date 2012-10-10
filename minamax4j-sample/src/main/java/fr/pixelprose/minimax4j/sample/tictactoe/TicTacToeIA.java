package fr.pixelprose.minimax4j.sample.tictactoe;

import java.util.ArrayList;
import java.util.List;

import fr.pixelprose.minimax4j.Difficulty;
import fr.pixelprose.minimax4j.IA;

public class TicTacToeIA extends IA<TicTacToeMove> {

    private static final int FREE       = 0;
    private static final int PLAYER_X   = 1; // X
    private static final int PLAYER_O   = 2; // O
    
    private static final int GRID_SIZE  = 3;
    
    /** The grid */
    private final int[][] grid;
    
    private int currentPlayer;
    
    private TicTacToeDifficulty difficulty;

    public TicTacToeIA(int depth) {
        super(Algorithm.MINIMAX);
        this.difficulty = new TicTacToeDifficulty(depth);
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        newGame();
    }
    
    public void newGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = FREE;
            }
        }
        // X start to play
        currentPlayer = PLAYER_X;
    }

    @Override
    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public boolean isOver() {
        return 
            (grid[0][0] == grid[0][1] && grid[0][0] == grid[0][2] && grid[0][0] != FREE)
            ||
            (grid[1][0] == grid[1][1] && grid[1][0] == grid[1][2] && grid[1][0] != FREE)
            ||
            (grid[2][0] == grid[2][1] && grid[2][0] == grid[2][2] && grid[2][0] != FREE)
            ||
            (grid[0][0] == grid[1][0] && grid[0][0] == grid[2][0] && grid[0][0] != FREE)
            ||
            (grid[0][1] == grid[1][1] && grid[0][1] == grid[2][1] && grid[0][1] != FREE)
            ||
            (grid[0][2] == grid[1][2] && grid[0][2] == grid[2][2] && grid[0][2] != FREE)
            ||
            (grid[0][0] == grid[1][1] && grid[0][0] == grid[2][2] && grid[0][0] != FREE)
            ||
            (grid[0][2] == grid[1][1] && grid[0][2] == grid[2][0] && grid[0][2] != FREE);
    }

    @Override
    public void makeMove(TicTacToeMove move) {
        grid[move.getX()][move.getY()] = currentPlayer;
        next();
    }

    @Override
    public void unmakeMove(TicTacToeMove move) {
        grid[move.getX()][move.getY()] = FREE;
        previous();
    }

    @Override
    public List<TicTacToeMove> getPossibleMoves() {
        List<TicTacToeMove> moves = new ArrayList<TicTacToeMove>(9);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == FREE) {
                    moves.add(new TicTacToeMove(i, j, currentPlayer));
                }
            }
        }
        // moves can be sorted to optimize alpha-beta pruning
        // {1,1} is always the best move when available
        return moves;
    }

    @Override
    public double evaluate() {
        if (isOver()) {
            // 2 for the win
            return 2;
        }
        if (grid[1][1] == currentPlayer) {
            // 1 for {1,1}
            return 1;
        }
        // don't care of other moves
        return 0;
    }

    @Override
    public double minEvaluateValue() {
        // evaluate return either 0, 1 or 2
        return -1;
    }

    @Override
    public double maxEvaluateValue() {
        // evaluate return either 0, 1 or 2
        return 3;
    }

    @Override
    public void next() {
        currentPlayer = 3 - currentPlayer;
    }

    @Override
    public void previous() {
        currentPlayer = 3 - currentPlayer;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("+-+-+-+\n");
        sb.append("|");
        sb.append(grid[0][0] == FREE ? " " : (grid[0][0] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[0][1] == FREE ? " " : (grid[0][1] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[0][2] == FREE ? " " : (grid[0][2] == PLAYER_O ? "O" : "X"));
        sb.append("|\n");
        sb.append("+-+-+-+\n");
        sb.append("|");
        sb.append(grid[1][0] == FREE ? " " : (grid[1][0] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[1][1] == FREE ? " " : (grid[1][1] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[1][2] == FREE ? " " : (grid[1][2] == PLAYER_O ? "O" : "X"));
        sb.append("|\n");
        sb.append("+-+-+-+\n");
        sb.append("|");
        sb.append(grid[2][0] == FREE ? " " : (grid[2][0] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[2][1] == FREE ? " " : (grid[2][1] == PLAYER_O ? "O" : "X"));
        sb.append("|");
        sb.append(grid[2][2] == FREE ? " " : (grid[2][2] == PLAYER_O ? "O" : "X"));
        sb.append("|\n");
        sb.append("+-+-+-+");
        return sb.toString();
    }

}
