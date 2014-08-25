package fr.avianey.minimax4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012, 2013, 2014 Antoine Vianey
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

public abstract class ParallelMinimax<M extends Move> extends Minimax<M> {
    
    private final ForkJoinPool pool;
    
    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm<br/>
     * {@link Algorithm#NEGASCOUT} performs slowly on several tests at the moment...
     */
    public ParallelMinimax() {
        this(Algorithm.NEGAMAX);
    }
    
    /**
     * Creates a new IA using the provided algorithm
     * @param algo The decision rule to use
     * @see Algorithm
     */
    public ParallelMinimax(Algorithm algo) {
        super(algo);
        pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Get the best {@link Move} for the given search depth
     * @param depth The search depth (must be > 0)
     * @return
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper;
        Cutoff cutoff = new Cutoff(-maxEvaluateValue(), maxEvaluateValue());
        // TODO : create pool in constructor
        switch (getAlgo()) {
        default:
        case NEGAMAX:
            wrapper = pool.invoke(new NegamaxMasterTask<>(this, depth, cutoff));
            break;
        }
        return wrapper.move;
    }
    
    private double negamax(final MoveWrapper<M> wrapper, final int depth, final Cutoff cutoff) throws InterruptedException, ExecutionException {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
        	next();
        	double score = negamaxScore(depth, cutoff);
        	previous();
        	return score;
        } else {
            boolean first = true;
            double currentBest = 0;
            LinkedList<NegamaxTask<M>> tasks = new LinkedList<>();
            for (M move : moves) {
                NegamaxTask<M> task = new NegamaxTask<>(this.clone(), move, depth, cutoff);
                if (first) {
                    // young brother wait...
                    // reduce alpha beta window
                    // assume it's the best possible move
                    first = false;
                    task.fork().get();
                    if (wrapper != null) {
                        wrapper.move = move;
                        currentBest = task.getRawResult();
                    }
                } else {
                    task.fork();
                    tasks.add(task);
                }
            }
            // await termination of all brothers
            // once all done alpha == best score
            for (NegamaxTask<M> task : tasks) {
                if (task.get() != null // not a cutoff
                        && task.getRawResult() > currentBest 
                        && wrapper != null) {
                    currentBest = task.getRawResult();
                    wrapper.move = task.move;
                }
            }
            return cutoff.alpha;
        }
    }

    protected double negamaxScore(final int depth, final Cutoff cutoff) throws InterruptedException, ExecutionException {
		return -negamax(null, depth - 1, cutoff.inverse());
	}
    
    @Override
    public abstract ParallelMinimax<M> clone();
    
    static class NegamaxMasterTask<M extends Move> extends RecursiveTask<MoveWrapper<M>> {

        private static final long serialVersionUID = 1L;
        
        final ParallelMinimax<M> minimax;
        final MoveWrapper<M> wrapper = new MoveWrapper<>();
        final int depth;
        final Cutoff cutoff;
        
        public NegamaxMasterTask(ParallelMinimax<M> minimax, int depth, Cutoff cutoff) {
            this.depth = depth;
            this.cutoff = cutoff;
            this.minimax = minimax;
        }
        
        @Override
        protected MoveWrapper<M> compute() {
            try {
                minimax.negamax(wrapper, depth, cutoff);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return wrapper;
        }
        
    }
    
    static class NegamaxTask<M extends Move> extends RecursiveTask<Double> {

        private static final long serialVersionUID = 1L;
        
        final int depth;
        final ParallelMinimax<M> minimax;
        final Cutoff cutoff;
        final M move;
        
        NegamaxTask(ParallelMinimax<M> minimax, M move, int depth, Cutoff cutoff) {
            this.depth = depth;
            this.minimax = minimax;
            this.cutoff = cutoff;
            this.move = move;
        }
        
        @Override
        protected Double compute() {
            minimax.makeMove(move);
            Double score = null;
            try {
                score = minimax.negamaxScore(depth, cutoff);
            } catch (InterruptedException | ExecutionException e) {}
            minimax.unmakeMove(move);
            return cutoff.check(score);
        }
        
    }
    
    static class Cutoff {

        public volatile Double alpha;
        public volatile Double beta;
        
        public Cutoff(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
        
        public Cutoff inverse() {
            return new Cutoff(-beta, -alpha);
        }

        public Double check(double score) {
            if (score > alpha) {
                synchronized (this) {
                    // double checked locking
                    if (score > alpha) {
                        alpha = score;
                        if (alpha >= beta) {
                            // cutoff
                            return null;
                        }
                    }
                }
            }
            return score;
        }
        
    }
    
    
}
