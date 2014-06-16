package fr.avianey.minimax4j.sample.tictactoe;

import java.util.ArrayList;
import java.util.List;

import fr.avianey.minimax4j.Difficulty;
import fr.avianey.minimax4j.TranspositionIA;

/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012 Antoine Vianey
 * 
 * minimax4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with minimax4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */

/**
 * Simple TicTacToe IA to showcase the API. 
 * 
 * @author antoine vianey
 */
public class TicTacToeTranspositionIA extends TranspositionIA<TicTacToeMove, Integer, Integer> {

    static final int FREE       = 0;
    static final int PLAYER_X   = 1; // X : 01
    static final int PLAYER_O   = 2; // O : 10

    private static final int GRID_SIZE  = 3;
    private static final int MAX_TURN	= GRID_SIZE * GRID_SIZE;
    
    /** The grid */
    private final int[][] grid;
    
    private int currentPlayer;
    private int turn = 0;
    private int hash = 0;
    
    private TicTacToeDifficulty difficulty;

    public TicTacToeTranspositionIA(Algorithm algo, int depth) {
        super(algo);
        this.difficulty = new TicTacToeDifficulty(depth);
        this.grid = new int[GRID_SIZE][GRID_SIZE];
        newGame();
    }
    
    public void newGame() {
    	hash = 0;
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
    public Difficulty getDifficulty() {
        return difficulty;
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
        grid[move.getX()][move.getY()] = move.getPlayer();
        hash = hash ^ (move.getPlayer() << ((move.getX() + GRID_SIZE * move.getY()) * 2));
        turn++;
        next();
    }

    @Override
    public void unmakeMove(TicTacToeMove move) {
        grid[move.getX()][move.getY()] = FREE;
        hash = hash ^ (move.getPlayer() << ((move.getX() + GRID_SIZE * move.getY()) * 2));
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

    /*===============================*
     * TRANSPOSITION TABLE BACKED IA *
     *===============================*/

	@Override
	public Integer getTransposition() {
		return hash;
	}

	@Override
	public Integer getGroup() {
		// as moves increase over turns
		// we don't need to keep transposition from previous turns
		return turn;
	}
	
	@Override
    public boolean clearGroupsAfterSearch() {
		// remove useless transposition after search
		// groups reflect turns and as players can't
		// go back on a move in the next turn, 
		// transpositions from previous turns are useless
    	return true;
    }
	
	@Override
	public TicTacToeMove getBestMove() {
		TicTacToeMove move = super.getBestMove();
		// clear the content of the transposition table
		// unless it reached the max depth... if not
		// using a known transposition value will lead to a loss
		// of search depth as we will use the result of an evaluation
		// with a lower depth of prediction !
		// we may have used informations from the transposition table
		// to order available moves as an optimization for alpha-beta cut-off
		if (turn + getDifficulty().getDepth() < MAX_TURN) {
			// use with caution
			super.clearTranspositionTable();
		}
//		super.clearTranspositionTable();
		return move;
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

}
