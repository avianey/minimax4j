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

    Logic() {}

    boolean isOver(BaseState state) {
        return state.getTurn() == GRID_SIZE;
    }

    List<IAMove> getPossibleMoves(BaseState state) {
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

    double evaluate(BaseState state) {
        if (isOver(state)) {
            // check win
            int[] scores = new int[2];
            int index = 0;
            for (double cell : state.getGrid()) {
                if (cell != EMPTY_CELL) {
                    scores[(int) floor(cell)] += GRID_VALUES[index] + cell - floor(cell);
                }
                index++;
            }
            for (int i = 0; i < 2; i++) {
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
