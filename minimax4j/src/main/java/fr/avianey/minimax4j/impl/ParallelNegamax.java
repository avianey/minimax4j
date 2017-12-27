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
package fr.avianey.minimax4j.impl;

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.Move;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static java.lang.Runtime.getRuntime;

/**
 * A {@link IA} implementation that distribute the tree exploration across processors.<br/>
 * 
 * @param <M>  Implementation of the Move interface to use
 * @author antoine vianey
 */
public abstract class ParallelNegamax<M extends Move> implements IA<M>, Cloneable {
    
    private ForkJoinPool pool;

    /**
     * Creates a new ParallelNegamax using {@link Runtime#availableProcessors()} for parallelism.
     */
    public ParallelNegamax() {
        this(getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new ParallelNegamax using the given parallelism.
     * @param parallelism how many workers should be used for computation
     */
    public ParallelNegamax(int parallelism) {
        if (parallelism <= 0) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " MUST use at least one processor.");
        }
        this.pool = new ForkJoinPool(parallelism);
    }
    
    /**
     * Creates a new IA with the same parallelism as the given instance.<br/>
     * This constructor SHOULD be used when creating clones as it skip {@link ForkJoinPool} configuration.
     * @param from
     */
    protected ParallelNegamax(ParallelNegamax<M> from) {
        this(from.pool.getParallelism());
    }
    
    /**
     * Get the best {@link Move} for the given search depth<br/>
     * This method SHOULD be called from one thread at the time.
     * @param depth The search depth (must be > 0)
     * @return The best possible move
     */
    public List<M> getBestMoves(final int depth, List<M> orderedMoves) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        pool.invoke(new NegamaxAction<M>(this, orderedMoves, depth, -maxEvaluateValue(), maxEvaluateValue()));
        Collections.sort(orderedMoves);
        return orderedMoves;
    }
    
    @Override
    public abstract ParallelNegamax<M> clone();

    private static final class NegamaxAction<M extends Move> extends RecursiveTask<Double> {

        private static final long serialVersionUID = 1L;
        
        private final List<M> initialMoves;
        private final ParallelNegamax<M> minimax;
        private final int depth;
        private final double alpha;
        private final double beta;

        NegamaxAction(ParallelNegamax<M> minimax, List<M> initialMoves, int depth, double alpha, double beta) {
            this.initialMoves = initialMoves;
            this.depth = depth;
            this.minimax = minimax;
            this.alpha = alpha;
            this.beta = beta;
        }

        @Override
        protected Double compute() {
            return negamax(initialMoves, depth, alpha, beta);
        }
        
        private double negamax(final List<M> initialMoves, final int depth, double alpha, double beta) {
            if (depth == 0 || minimax.isOver()) {
                return minimax.evaluate();
            }
            Iterator<M> moves = (initialMoves != null ? initialMoves : minimax.getPossibleMoves()).iterator();
            if (moves.hasNext()) {
                // young brother wait
                // reduce alpha beta window
                // assume its the best possible move
                M move = moves.next();
                minimax.makeMove(move);
                double score = -negamax(null, depth - 1, -beta, -alpha);
                minimax.unmakeMove(move);
                if (initialMoves != null) {
                    move.value = score;
                }
                if (score > alpha) {
                    alpha = score;
                    if (alpha >= beta) {
                        // cutoff
                        return alpha;
                    }
                }
                if (moves.hasNext()) {
                    Collection<NegamaxAction<M>> tasks = new LinkedList<>();
                    do {
                        // create sub tree exploration tasks
                        move = moves.next();
                        ParallelNegamax<M> clone = minimax.clone();
                        clone.makeMove(move);
                        tasks.add(new NegamaxAction<>(clone, null, depth - 1, -beta, -alpha));
                    } while (moves.hasNext());
                    // dispatch tasks across workers
                    // and wait for completion...
                    invokeAll(tasks);
                    // await termination of all brothers
                    // once all done alpha == best score
                    for (NegamaxAction<M> task : tasks) {
                        score = -task.getRawResult();
                        if (initialMoves != null) {
                            move.value = score;
                        }
                        if (score > alpha) {
                            alpha = score;
                            if (alpha >= beta) {
                                // task lead to a cutoff...
                                // we don't care of other brothers
                                break;
                            }
                        }
                    }
                }
            } else {
                minimax.next();
                alpha = -negamax(null, depth - 1, -beta, -alpha);
                minimax.previous();
            }
            // sub tree done !
            return alpha;
        }
        
    }
    
}
