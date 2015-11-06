/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *
 * The MIT License (MIT)
 *
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
package fr.avianey.minimax4j.ia;

import java.util.Arrays;

import static fr.avianey.minimax4j.ia.Logic.EMPTY_CELL;
import static fr.avianey.minimax4j.ia.Logic.GRID_SIZE;
import static fr.avianey.minimax4j.ia.Logic.GRID_VALUES;

/**
 * Wrapper to reuse state across IA implementations.
 * This class <b>IS NOT</b> safe to use in multiple concurrent threads.
 *
 * @author antoine vianey
 */
final class State {

    private final int nbPlayers;
    private final double[] grid = new double[GRID_SIZE];
    private int turn;
    private int currentPlayer;

    State(int nbPlayers) {
        this.nbPlayers = nbPlayers;
        reset();
    }

    void reset() {
        Arrays.fill(grid, EMPTY_CELL);
        turn = 0;
        currentPlayer = 0;
    }

    // for parallel minimax
    @Override
    public State clone() {
        State clone = new State(nbPlayers);
        clone.turn = turn;
        clone.currentPlayer = currentPlayer;
        System.arraycopy(grid, 0, clone.grid, 0, GRID_SIZE);
        return clone;
    }

    // for transposition minimax
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;

        if (turn != state.turn) return false;
        if (currentPlayer != state.currentPlayer) return false;
        return Arrays.equals(grid, state.grid);
    }

    // for transposition minimax
    @Override
    public int hashCode() {
        int result = grid != null ? Arrays.hashCode(grid) : 0;
        result = 31 * result + turn;
        result = 31 * result + currentPlayer;
        return result;
    }

    void makeMove(IAMove move) {
        // take position
        // add the fraction of the score associated with the turn
        grid[move.getPosition()] = currentPlayer + (((GRID_VALUES[move.getPosition()] / (double) GRID_SIZE) * (GRID_SIZE - turn - 1)) / (double) GRID_SIZE);
        turn++;
        // move to next player
        next();
    }

    void unmakeMove(IAMove move) {
        // free position
        grid[move.getPosition()] = EMPTY_CELL;
        turn--;
        // restore previous player
        previous();
    }

    void next() {
        currentPlayer++;
        if (currentPlayer >= nbPlayers) {
            currentPlayer = 0;
        }
    }

    void previous() {
        currentPlayer--;
        if (currentPlayer < 0) {
            currentPlayer = nbPlayers - 1;
        }
    }

    double[] getGrid() {
        return grid;
    }

    int getTurn() {
        return turn;
    }

    int getCurrentPlayer() {
        return currentPlayer;
    }
}
