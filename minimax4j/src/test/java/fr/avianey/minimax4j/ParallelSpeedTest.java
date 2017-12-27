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
import fr.avianey.minimax4j.impl.Negamax;
import fr.avianey.minimax4j.impl.ParallelNegamax;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ParallelSpeedTest {

    private static final int TURNS = 64;
    private static final int COST = 1; // 1 ms
    private static final String FORMAT = "|\t%s\t|\t%s\t|\t%d\t|\t%d\t|\t%d\t|\t%d\t|\t%d\t|\t%d\t|\t%d\t|\t%d\t|";

    private TestIA ia;

    private final int depth;

    public static Collection<Object[]> params() {
        return Arrays.asList(
                new Object[]{NegamaxIA.class, 3, 4, 1, 1, 1, 1, 1, 1},
                new Object[]{NegamaxIA.class, 3, 4, 2, 1, 1, 1, 1, 1},
                new Object[]{NegamaxIA.class, 3, 4, 1, 2, 1, 1, 1, 1},
                new Object[]{NegamaxIA.class, 3, 4, 1, 1, 2, 1, 1, 1},
                new Object[]{NegamaxIA.class, 3, 4, 1, 1, 1, 2, 1, 1},
                new Object[]{NegamaxIA.class, 3, 4, 1, 1, 1, 1, 2, 1},
                new Object[]{NegamaxIA.class, 3, 4, 1, 1, 1, 1, 1, 2},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 1, 1, 1, 1, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 2, 1, 1, 1, 1, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 2, 1, 1, 1, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 1, 2, 1, 1, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 1, 1, 2, 1, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 1, 1, 1, 2, 1},
                new Object[]{ParallelTestIA.class, 3, 4, 1, 1, 1, 1, 1, 2}
        );
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        for (Object[] params : params()) {
            ParallelSpeedTest speedTest = new ParallelSpeedTest(
                    (Class<? extends IA<VoidMove>>) params[0],
                    (int) params[1], (int) params[2],
                    (long) params[3], (long) params[5], (long) params[5],
                    (long) params[6], (long) params[7], (long) params[8]);
            speedTest.warmup();
            speedTest.run();
        }
    }

    public ParallelSpeedTest(Class<? extends IA<VoidMove>> iaClass,
                             int depth, int branchingFactor,
                             long getPossibleMovesCost, long makeMoveCost, long unmakeMoveCost,
                             long evaluateCost, long nextPreviousCost, long cloneCost) {
        this.depth = depth;
        if (NegamaxIA.class.equals(iaClass)) {
            ia = new NegamaxIA(branchingFactor,
                    COST * makeMoveCost, COST * unmakeMoveCost,
                    COST * getPossibleMovesCost, COST * evaluateCost,
                    COST * nextPreviousCost, COST * cloneCost);
        }
        if (ParallelTestIA.class.equals(iaClass)) {
            ia = new ParallelTestIA(branchingFactor,
                    COST * makeMoveCost, COST * unmakeMoveCost,
                    COST * getPossibleMovesCost, COST * evaluateCost,
                    COST * nextPreviousCost, COST * cloneCost);
        }
    }

    public void warmup() throws IllegalAccessException, InstantiationException {
        ia.clear();
        dryRun((IA) ia);
    }

    public void run() throws IllegalAccessException, InstantiationException {
        Stopwatch watch = Stopwatch.createStarted();
        ia.clear();
        dryRun((IA) ia);
        watch.stop();
        System.out.println(String.format(FORMAT, ia.getClass().getSuperclass().getSimpleName(), watch,
                depth,
                ia.getBranchingFactor(),
                ia.getGetPossibleMovesCost(),
                ia.getMakeMoveCost(),
                ia.getUnmakeMoveCost(),
                ia.getEvaluateCost(),
                ia.getNextPreviousCost(),
                ia.getCloneCost()));
    }

    private void dryRun(IA<VoidMove> IA) {
        while (!IA.isOver()) {
            VoidMove move = IA.getBestMoves(depth).get(0);
            IA.makeMove(move);
        }
    }

    private static class VoidMove extends Move {}

    private static class VoidMoves extends AbstractList<VoidMove> {

        private final VoidMove move = new VoidMove();
        private final int size;

        private VoidMoves(int size) {
            this.size = size;
        }

        @Override
        public VoidMove get(int i) {
            return move;
        }

        @Override
        public int size() {
            return size;
        }
    }

    private interface TestIA {

        int getBranchingFactor();

        long getMakeMoveCost();

        long getUnmakeMoveCost();

        long getGetPossibleMovesCost();

        long getEvaluateCost();

        long getNextPreviousCost();

        long getCloneCost();

        void clear();
    }

    private static class NegamaxIA extends Negamax<VoidMove> implements TestIA {

        private final int branchingFactor;
        private final long makeMoveCost;
        private final long unmakeMoveCost;
        private final long getPossibleMovesCost;
        private final long evaluateCost;
        private final long nextPreviousCost;
        private final long cloneCost;

        private int turn = 0;

        public NegamaxIA(int branchingFactor,
                         long makeMoveCost, long unmakeMoveCost, long getPossibleMovesCost,
                         long evaluateCost, long nextPreviousCost, long cloneCost) {
            this.branchingFactor = branchingFactor;
            this.makeMoveCost = makeMoveCost;
            this.unmakeMoveCost = unmakeMoveCost;
            this.getPossibleMovesCost = getPossibleMovesCost;
            this.evaluateCost = evaluateCost;
            this.nextPreviousCost = nextPreviousCost;
            this.cloneCost = cloneCost;
        }

        public void clear() {
            this.turn = 0;
        }

        @Override
        public boolean isOver() {
            return turn == TURNS;
        }

        @Override
        public void makeMove(VoidMove move) {
            simulateCost(makeMoveCost);
            turn++;
        }

        @Override
        public void unmakeMove(VoidMove move) {
            simulateCost(unmakeMoveCost);
            turn--;
        }

        @Override
        public List<VoidMove> getPossibleMoves() {
            simulateCost(getPossibleMovesCost);
            return new VoidMoves(branchingFactor);
        }

        @Override
        public double evaluate() {
            simulateCost(evaluateCost);
            return 0;
        }

        @Override
        public double maxEvaluateValue() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void next() {
            simulateCost(nextPreviousCost);
        }

        @Override
        public void previous() {
            simulateCost(nextPreviousCost);
        }

        @Override
        public int getBranchingFactor() {
            return branchingFactor;
        }

        @Override
        public long getMakeMoveCost() {
            return makeMoveCost;
        }

        @Override
        public long getUnmakeMoveCost() {
            return unmakeMoveCost;
        }

        @Override
        public long getGetPossibleMovesCost() {
            return getPossibleMovesCost;
        }

        @Override
        public long getEvaluateCost() {
            return evaluateCost;
        }

        @Override
        public long getNextPreviousCost() {
            return nextPreviousCost;
        }

        @Override
        public long getCloneCost() {
            return cloneCost;
        }
    }

    private static class ParallelTestIA extends ParallelNegamax<VoidMove> implements TestIA {

        private final int branchingFactor;
        private final long makeMoveCost;
        private final long unmakeMoveCost;
        private final long getPossibleMovesCost;
        private final long evaluateCost;
        private final long nextPreviousCost;
        private final long cloneCost;

        private int turn = 0;

        public ParallelTestIA(int branchingFactor,
                      long makeMoveCost, long unmakeMoveCost, long getPossibleMovesCost,
                      long evaluateCost, long nextPreviousCost, long cloneCost) {
            this.branchingFactor = branchingFactor;
            this.makeMoveCost = makeMoveCost;
            this.unmakeMoveCost = unmakeMoveCost;
            this.getPossibleMovesCost = getPossibleMovesCost;
            this.evaluateCost = evaluateCost;
            this.nextPreviousCost = nextPreviousCost;
            this.cloneCost = cloneCost;
        }

        public void clear() {
            this.turn = 0;
        }

        @Override
        public boolean isOver() {
            return turn == TURNS;
        }

        @Override
        public void makeMove(VoidMove move) {
            simulateCost(makeMoveCost);
            turn++;
        }

        @Override
        public void unmakeMove(VoidMove move) {
            simulateCost(unmakeMoveCost);
            turn--;
        }

        @Override
        public List<VoidMove> getPossibleMoves() {
            simulateCost(getPossibleMovesCost);
            return new VoidMoves(branchingFactor);
        }

        @Override
        public double evaluate() {
            simulateCost(evaluateCost);
            return 0;
        }

        @Override
        public double maxEvaluateValue() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void next() {
            simulateCost(nextPreviousCost);
        }

        @Override
        public void previous() {
            simulateCost(nextPreviousCost);
        }

        @Override
        public ParallelNegamax<VoidMove> clone() {
            simulateCost(cloneCost);
            ParallelTestIA ia = new ParallelTestIA(branchingFactor, makeMoveCost, unmakeMoveCost, getPossibleMovesCost, evaluateCost, nextPreviousCost, cloneCost);
            ia.turn = turn;
            return ia;
        }

        @Override
        public int getBranchingFactor() {
            return branchingFactor;
        }

        @Override
        public long getMakeMoveCost() {
            return makeMoveCost;
        }

        @Override
        public long getUnmakeMoveCost() {
            return unmakeMoveCost;
        }

        @Override
        public long getGetPossibleMovesCost() {
            return getPossibleMovesCost;
        }

        @Override
        public long getEvaluateCost() {
            return evaluateCost;
        }

        @Override
        public long getNextPreviousCost() {
            return nextPreviousCost;
        }

        @Override
        public long getCloneCost() {
            return cloneCost;
        }
    }

    private static void simulateCost(long cost) {
        try {
            Thread.sleep(cost);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}
