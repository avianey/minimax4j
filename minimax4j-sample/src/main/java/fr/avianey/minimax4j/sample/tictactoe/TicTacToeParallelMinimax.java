/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *
 * The MIT License (MIT)

 * Copyright (c) 2015 Antoine Vianey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fr.avianey.minimax4j.sample.tictactoe;

import java.util.ArrayList;
import java.util.List;

import fr.avianey.minimax4j.ParallelMinimax;

/**
 * Simple TicTacToe IA to showcase the API. 
 * 
 * @author antoine vianey
 */
public class TicTacToeParallelMinimax extends ParallelMinimax<TicTacToeMove> {

    static final int FREE       = 0;
    static final int PLAYER_X   = 1; // X
    static final int PLAYER_O   = 2; // O
    
    private static final int GRID_SIZE  = 3;
    
    /** The grid */
    private final int[][] grid;
    
    private int currentPlayer;
    private int turn = 0;

    public TicTacToeParallelMinimax(Algorithm algo) {
        super(algo);
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        newGame();
    }
    
    private TicTacToeParallelMinimax(Algorithm algo, int[][] grid, int turn, int currentPlayer) {
        this(algo);
        this.currentPlayer = currentPlayer;
        this.turn = turn;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                this.grid[i][j] = grid[i][j];
            }
        }
    }
    
    public void newGame() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = FREE;
            }
        }
        // X start to play
        currentPlayer = PLAYER_X;
        turn = 0;
    }

    @Override
    public boolean isOver() {
        return hasWon(PLAYER_O) || hasWon(PLAYER_X) || turn == 9;
    }
    
    private boolean hasWon(int player) {
        return 
            (player == grid[0][1] && player == grid[0][2] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[1][2] && player == grid[1][0])
            ||
            (player == grid[2][1] && player == grid[2][2] && player == grid[2][0])
            ||
            (player == grid[1][0] && player == grid[2][0] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[2][1] && player == grid[0][1])
            ||
            (player == grid[1][2] && player == grid[2][2] && player == grid[0][2])
            ||
            (player == grid[1][1] && player == grid[2][2] && player == grid[0][0])
            ||
            (player == grid[1][1] && player == grid[2][0] && player == grid[0][2]);
    }

    @Override
    public void makeMove(TicTacToeMove move) {
        grid[move.getX()][move.getY()] = currentPlayer;
        turn++;
        next();
    }

    @Override
    public void unmakeMove(TicTacToeMove move) {
        grid[move.getX()][move.getY()] = FREE;
        turn--;
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
        int eval = 0;
        if (hasWon(currentPlayer)) {
            // 2 for the win
            eval = 2;
        } else if (hasWon(3 - currentPlayer)) {
            // -2 for loosing
            eval = -2;
        } else if (grid[1][1] == currentPlayer) {
            // 1 for {1,1}
            eval = 1;
        } else if (grid[1][1] == 3 - currentPlayer) {
            // -1 for opponent {1,1}
            eval = -1;
        }
        return eval;
    }

    @Override
    public double maxEvaluateValue() {
        // evaluate return either -2, -1, 0, 1 or 2
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
        sb.append(grid[0][0] == FREE ? " " : (grid[0][0] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][0] == FREE ? " " : (grid[1][0] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][0] == FREE ? " " : (grid[2][0] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        sb.append(grid[0][1] == FREE ? " " : (grid[0][1] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][1] == FREE ? " " : (grid[1][1] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][1] == FREE ? " " : (grid[2][1] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        sb.append(grid[0][2] == FREE ? " " : (grid[0][2] == PLAYER_O ? "O" : "X"));
        sb.append(grid[1][2] == FREE ? " " : (grid[1][2] == PLAYER_O ? "O" : "X"));
        sb.append(grid[2][2] == FREE ? " " : (grid[2][2] == PLAYER_O ? "O" : "X"));
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public ParallelMinimax<TicTacToeMove> clone() {
        return new TicTacToeParallelMinimax(getAlgo(), grid, turn, currentPlayer);
    }

}
