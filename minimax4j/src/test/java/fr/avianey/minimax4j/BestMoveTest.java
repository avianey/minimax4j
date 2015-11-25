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
package fr.avianey.minimax4j;

import fr.avianey.minimax4j.ia.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class BestMoveTest {

    private final int depth;
    private final IA<IAMove> IA;

    public BestMoveTest(int depth, IA<IAMove> IA) {
        this.depth = depth;
        this.IA = IA;
    }

    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(
                // IA
                new Object[]{1, new BaseMinimax()},
                new Object[]{2, new BaseMinimax()},
                new Object[]{3, new BaseMinimax()},
                new Object[]{1, new BaseAlphaBeta()},
                new Object[]{2, new BaseAlphaBeta()},
                new Object[]{3, new BaseAlphaBeta()},
                new Object[]{1, new BaseNegamax()},
                new Object[]{2, new BaseNegamax()},
                new Object[]{3, new BaseNegamax()},
                // parallel IA
                new Object[]{1, new BaseParallelNegamax()},
                new Object[]{2, new BaseParallelNegamax()},
                new Object[]{3, new BaseParallelNegamax()},
                // transposition IA
                new Object[]{1, new TranspositionNegamaxNoCollision()},
                new Object[]{2, new TranspositionNegamaxNoCollision()},
                new Object[]{3, new TranspositionNegamaxNoCollision()}/*,
                new Object[]{4, new TranspositionNegamaxNoCollision()}*/
        );
    }

    @Test
    public void shouldBestMoveAlwaysReturnLastAvailableCell() {
        int cell = Logic.GRID_SIZE - 1;
        while (!IA.isOver()) {
            IAMove move = IA.getBestMove(depth);
            assertEquals("Best move must be highest available position in grid.", cell, move.getPosition());
            IA.makeMove(move);
            cell--;
        }
        assertEquals("When over, all cell should be taken.", -1, cell);
    }

}
