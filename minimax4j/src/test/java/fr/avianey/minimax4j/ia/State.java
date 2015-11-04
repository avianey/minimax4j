/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *
 * Copyright (C) 2012 - 2015 Antoine Vianey
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
