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
import fr.avianey.minimax4j.ia.BaseNegamax;
import fr.avianey.minimax4j.ia.Cleanable;
import fr.avianey.minimax4j.ia.IAMove;
import fr.avianey.minimax4j.ia.TranspositionNegamaxWithGroup;

import java.util.Arrays;
import java.util.Collection;

public class TranspositionSpeedTest {

    private static final String FORMAT = "|\t%s\t|\t%s\t|\t%d\t|";

    private IA<?> ia;

    private final int depth;

    public static Collection<Object[]> params() {
        return Arrays.asList(
                new Object[]{BaseNegamax.class, 3},
                new Object[]{TranspositionNegamaxWithGroup.class, 3}
        );
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        for (Object[] params : params()) {
            TranspositionSpeedTest speedTest = new TranspositionSpeedTest(
                    (Class<? extends IA<IAMove>>) params[0],
                    (int) params[1]);
            speedTest.warmup();
            speedTest.run();
        }
    }

    public TranspositionSpeedTest(Class<? extends IA<IAMove>> iaClass, int depth) throws IllegalAccessException, InstantiationException {
        this.depth = depth;
        ia = iaClass.newInstance();
    }

    public void warmup() throws IllegalAccessException, InstantiationException {
        dryRun((IA<IAMove>) ia);
    }

    public void run() throws IllegalAccessException, InstantiationException {
        Stopwatch watch = Stopwatch.createStarted();
        ((Cleanable) ia).clean();
        dryRun((IA<IAMove>) ia);
        watch.stop();
        System.out.println(String.format(FORMAT,
                ia.getClass().getSuperclass().getSimpleName(),
                watch,
                depth));
    }

    private void dryRun(IA<IAMove> IA) {
        while (!IA.isOver()) {
            IA.makeMove(IA.getBestMove(depth));
        }
    }

}
