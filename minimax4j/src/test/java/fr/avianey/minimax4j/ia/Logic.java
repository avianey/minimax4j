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

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.floor;

/**
 * Stateless Wrapper to reuse logic across IA implementations.
 * This class <b>IS</b> safe to use in multiple concurrent threads.
 *
 * @author antoine vianey
 */
public final class Logic {

    public static final int SIZE = 8;
    public static final int GRID_SIZE = SIZE * SIZE;

    static final int MAX_SCORE = ((GRID_SIZE - 1) * GRID_SIZE) / 2;
    static final int EMPTY_CELL = -1;
    static final int[] GRID_VALUES = new int[GRID_SIZE];
    static {
        for (int i = 0; i < GRID_SIZE; i++) {
            GRID_VALUES[i] = i;
        }
    }

    private final int nbPlayers;

    Logic(int nbPlayers) {
        this.nbPlayers = nbPlayers;
    }

    boolean isOver(State state) {
        return state.getTurn() == GRID_SIZE;
    }

    List<IAMove> getPossibleMoves(State state) {
        LinkedList<IAMove> moves = new LinkedList<>();
        int index = 0;
        for (double cell : state.getGrid()) {
            if (cell == EMPTY_CELL) {
                moves.add(new IAMove(index));
            }
            index++;
        }
        return moves;
    }

    double evaluate(State state) {
        if (isOver(state)) {
            // check win
            int[] scores = new int[nbPlayers];
            int index = 0;
            for (double cell : state.getGrid()) {
                if (cell != EMPTY_CELL) {
                    scores[(int) floor(cell)] += GRID_VALUES[index] + cell - floor(cell);
                }
                index++;
            }
            for (int i = 0; i < nbPlayers; i++) {
                if (i != state.getCurrentPlayer() && scores[state.getCurrentPlayer()] < scores[i]) {
                    // player i win
                    return -MAX_SCORE + scores[state.getCurrentPlayer()];
                }
            }
            // current player win
            return MAX_SCORE + scores[state.getCurrentPlayer()];
        }

        // maximize position
        int diff = 0;
        int index = 0;
        for (double cell : state.getGrid()) {
            if (floor(cell) == state.getCurrentPlayer()) {
                // current player cell
                diff += GRID_VALUES[index] + cell - floor(cell);
            } else if (cell != EMPTY_CELL) {
                // opponent cell
                diff -= GRID_VALUES[index];
            }
            index++;
        }

        return diff;
    }

    double maxEvaluateValue() {
        return Integer.MAX_VALUE;
    }

}
