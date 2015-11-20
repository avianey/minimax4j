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
class BaseState implements Cleanable {

    protected final double[] grid = new double[GRID_SIZE];
    protected int turn;
    protected int currentPlayer;

    BaseState() {
        clean();
    }

    @Override
    public void clean() {
        Arrays.fill(grid, EMPTY_CELL);
        turn = 0;
        currentPlayer = 0;
    }

    // for parallel minimax
    @Override
    public BaseState clone() {
        BaseState clone = new BaseState();
        clone.turn = turn;
        clone.currentPlayer = currentPlayer;
        System.arraycopy(grid, 0, clone.grid, 0, GRID_SIZE);
        return clone;
    }

    void makeMove(IAMove move) {
        // take position
        grid[move.getPosition()] = currentPlayer;
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
        if (currentPlayer > 1) {
            currentPlayer = 0;
        }
    }

    void previous() {
        currentPlayer--;
        if (currentPlayer < 0) {
            currentPlayer = 1;
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
