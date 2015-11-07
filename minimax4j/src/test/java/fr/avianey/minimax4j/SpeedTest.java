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

import com.google.common.base.Stopwatch;
import fr.avianey.minimax4j.ia.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static fr.avianey.minimax4j.Minimax.Algorithm.*;
import static org.junit.Assert.assertEquals;

public class SpeedTest {

    private static final int DEPTH = 3;

    private Minimax<IAMove> minimax;
    private Minimax<IAMove> parallel;

    @Before
    public void setup() {
        minimax = new IA(NEGAMAX, 2);
        parallel = new ParallelIA(NEGAMAX, 2);
    }

    @Test
    public void execute() {
        System.out.println("Available processors : " + Runtime.getRuntime().availableProcessors());
        run(minimax);
        run(parallel);
    }

    public void run(Minimax<IAMove> minimax) {
        // warmup
        dryRun(minimax);
        ((Cleanable) minimax).clean();
        // test
        Stopwatch watch = Stopwatch.createStarted();
        dryRun(minimax);
        watch.stop();
        System.out.println(minimax.getClass().getSimpleName() + " : " + watch);
    }

    private void dryRun(Minimax<IAMove> minimax) {
        while (!minimax.isOver()) {
            IAMove move = minimax.getBestMove(DEPTH);
            minimax.makeMove(move);
        }
    }

}
