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

import static fr.avianey.minimax4j.ia.Logic.GRID_SIZE;
import static fr.avianey.minimax4j.ia.Logic.GRID_VALUES;

/**
 * Wrapper to reuse state across IA implementations.
 * This class <b>IS NOT</b> safe to use in multiple concurrent threads.
 *
 * @author antoine vianey
 */
class WeightedState extends BaseState {

    WeightedState() {
        super();
    }

    void makeMove(IAMove move) {
        super.makeMove(move);
        // add the fraction of the score associated with the turn
        // turn has been incremented by super so (GRID_SIZE - (turn - 1) - 1)
        grid[move.getPosition()] += (((GRID_VALUES[move.getPosition()] / (double) GRID_SIZE) * (GRID_SIZE - turn)) / (double) GRID_SIZE);
    }

}
