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

import static fr.avianey.minimax4j.Minimax.Algorithm.NEGAMAX;
import static java.lang.Runtime.getRuntime;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * A {@link Minimax} implementation that distribute the tree exploration across processors.<br/>
 * 
 * @param <M>
 * @author antoine vianey
 */
public abstract class ParallelMinimax<M extends Move> extends BasicMinimax<M> implements Cloneable {
    
    private ForkJoinPool pool;

    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm<br/>
     * {@link Algorithm#NEGASCOUT} performs slowly in case of a weak move ordering...
     */
    public ParallelMinimax() {
        this(NEGAMAX, getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm and the given parallelism.
     * @param parallelism
     */
    public ParallelMinimax(int parallelism) {
        this(NEGAMAX, parallelism);
    }
    
    /**
     * Creates a new IA using the provided algorithm and {@link Runtime#availableProcessors()} for parallelism.
     * @param algo The decision rule to use
     * @see Algorithm
     */
    public ParallelMinimax(Algorithm algo) {
        this(algo, getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new IA using the provided algorithm and the given parallelism.
     * @param algo
     * @param parallelism
     */
    public ParallelMinimax(Algorithm algo, int parallelism) {
        super(algo);
        if (parallelism <= 0) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " MUST use at least one processor.");
        }
        this.pool = new ForkJoinPool(parallelism);
    }
    
    /**
     * Creates a new IA with the same {@link Algorithm} as the given instance.<br/>
     * This constructor SHOULD be used when creating clones as it skip {@link ForkJoinPool} configuration.
     * @param from
     */
    protected ParallelMinimax(ParallelMinimax<M> from) {
        super(from.getAlgorithm());
    }
    
    /**
     * Get the best {@link Move} for the given search depth<br/>
     * This method SHOULD be called from one thread at the time.
     * @param depth The search depth (must be > 0)
     * @return The best possible move
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        switch (getAlgorithm()) {
        default:
        case NEGAMAX:
            pool.invoke(new NegamaxAction<>(this, wrapper, null, depth, -maxEvaluateValue(), maxEvaluateValue()));
            break;
        }
        return wrapper.move;
    }
    
    @Override
    public abstract ParallelMinimax<M> clone();

    private static final class NegamaxAction<M extends Move> extends RecursiveTask<Double> {

        private static final long serialVersionUID = 1L;
        
        private final MoveWrapper<M> wrapper;
        private final ParallelMinimax<M> minimax;
        private final int depth;
        private final M move;
        private final double alpha;
        private final double beta;

        public NegamaxAction(ParallelMinimax<M> minimax, MoveWrapper<M> wrapper, M move, int depth, double alpha, double beta) {
            this.wrapper = wrapper;
            this.depth = depth;
            this.minimax = minimax;
            this.move = move;
            this.alpha = alpha;
            this.beta = beta;
        }

        @Override
        protected Double compute() {
            try {
                return negamax(wrapper, depth, alpha, beta);
            } catch (InterruptedException | ExecutionException e) { /* ignore */ }
            return alpha;
        }
        
        private double negamax(final MoveWrapper<M> wrapper, final int depth, double alpha, double beta) throws InterruptedException, ExecutionException {
            if (depth == 0 || minimax.isOver()) {
                return minimax.evaluate();
            }
            Iterator<M> moves = minimax.getPossibleMoves().iterator();
            if (moves.hasNext()) {
                // young brother wait
                // reduce alpha beta window
                // assume its the best possible move
                M move = moves.next();
                minimax.makeMove(move);
                double score = -negamax(null, depth - 1, -beta, -alpha);
                minimax.unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    if (wrapper != null) {
                        wrapper.move = move;
                    }
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
                        ParallelMinimax<M> clone = minimax.clone();
                        clone.makeMove(move);
                        tasks.add(new NegamaxAction<>(clone, null, move, depth - 1, -beta, -alpha));
                    } while (moves.hasNext());
                    // dispatch tasks across workers
                    // and wait for completion...
                    invokeAll(tasks);
                    // await termination of all brothers
                    // once all done alpha == best score
                    for (NegamaxAction<M> task : tasks) {
                        if (-task.getRawResult() > alpha) {
                            alpha = -task.getRawResult();
                            if (wrapper != null) {
                                wrapper.move = task.move;
                            }
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
