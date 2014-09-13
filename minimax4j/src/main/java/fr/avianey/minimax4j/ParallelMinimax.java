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
    
    private static final ForkJoinPool pool = new ForkJoinPool();
        
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
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        Cutoff<M> cutoff = new Cutoff<>(-maxEvaluateValue(), maxEvaluateValue());
        switch (getAlgo()) {
        default:
        case NEGAMAX:
            pool.invoke(new NegamaxAction<M>(this, wrapper, null, depth, cutoff));
            break;
        }
        return wrapper.move;
    }
    
    @Override
    public abstract ParallelMinimax<M> clone();
        
    private static final class Cutoff<M extends Move> {

        public volatile Double alpha;
        public volatile Double beta;
        
        public Cutoff(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
        
        public Cutoff<M> inverse() {
            return new Cutoff<M>(-beta, -alpha);
        }

        public boolean check(double score, MoveWrapper<M> wrapper, M move) {
            if (score > alpha) {
                synchronized (this) {
                    // double checked locking
                    if (score > alpha) {
                        alpha = score;
	                    if (wrapper != null) {
	                        wrapper.move = move;
	                    }
                        if (alpha >= beta) {
                            // cutoff
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
    }
    
    private static final class NegamaxAction<M extends Move> extends RecursiveTask<Double> {

		private static final long serialVersionUID = 1L;
		
		private final MoveWrapper<M> wrapper;
		private final ParallelMinimax<M> minimax;
		private final int depth;
		private final Cutoff<M> cutoff;
		private final M move;

		public NegamaxAction(ParallelMinimax<M> minimax, MoveWrapper<M> wrapper, M move, int depth, Cutoff<M> cutoff) {
    		this.wrapper = wrapper;
            this.depth = depth;
            this.minimax = minimax;
            this.cutoff = cutoff;
            this.move = move;
    	}

		@Override
		protected Double compute() {
			if (cutoff.alpha >= cutoff.beta) {
				return null;
			}
            try {
                return negamax(wrapper, depth, cutoff);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return cutoff.alpha;
		}
    	
	    private double negamax(final MoveWrapper<M> wrapper, final int depth, final Cutoff<M> cutoff) throws InterruptedException, ExecutionException {
	        if (depth == 0 || minimax.isOver()) {
	            return minimax.evaluate();
	        }
	        Collection<M> moves = minimax.getPossibleMoves();
	        if (moves.isEmpty()) {
	        	minimax.next();
	        	double score = -negamax(null, depth - 1, cutoff.inverse());
	        	minimax.previous();
	        	return score;
	        } else {
	            boolean first = true;
	            Collection<NegamaxAction<M>> tasks = new LinkedList<>();
	            for (M move : moves) {
	                if (first || pool.getQueuedTaskCount() > 0) {
	                    // young brother wait...
	                    // reduce alpha beta window
	                    // assume it's the best possible move
	                    first = false;
	                    minimax.makeMove(move);
	                    double score = -negamax(null, depth - 1, cutoff.inverse());
	                    minimax.unmakeMove(move);
	                    if (cutoff.check(score, wrapper, move)) {
	                    	break;
	                    }
	                } else {
	                	// at least one worker free
	                	// let send some work to it !
	                	ParallelMinimax<M> clone = minimax.clone();
	                	clone.makeMove(move);
	                	NegamaxAction<M> task = new NegamaxAction<M>(clone, null, move, depth - 1, cutoff.inverse());
	                    task.fork();
	                    tasks.add(task);
	                }
	            }
	            // await termination of all brothers
	            // once all done alpha == best score
	            for (NegamaxAction<M> task : tasks) {
	                if (task.join() == null || cutoff.check(-task.getRawResult(), wrapper, task.move)) {
	                    // tasks has cancel automatically or task lead to a cutoff...
	                	// we don't need to wait for other brothers
                		break;
                	}
	            }
	            // sub tree done !
	            return cutoff.alpha;
	        }
	    }
	    
    }
    
}
