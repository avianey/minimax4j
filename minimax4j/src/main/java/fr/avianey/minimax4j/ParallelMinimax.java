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

/**
 * A {@link Minimax} implementation that distribute the tree exploration across processors.<br/>
 * 
 * @author antoine vianey
 * @param <M>
 */
public abstract class ParallelMinimax<M extends Move> extends Minimax<M> {
    
	private ForkJoinPool pool;

    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm<br/>
     * {@link Algorithm#NEGASCOUT} performs slowly in case of a weak move ordering...
     */
    public ParallelMinimax() {
        this(Algorithm.NEGAMAX, Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm and the given parallelism.
     * @param parallelism
     */
    public ParallelMinimax(int parallelism) {
        this(Algorithm.NEGAMAX, parallelism);
    }
    
    /**
     * Creates a new IA using the provided algorithm and {@link Runtime#availableProcessors()} for parallelism.
     * @param algo The decision rule to use
     * @see Algorithm
     */
    public ParallelMinimax(Algorithm algo) {
        this(algo, Runtime.getRuntime().availableProcessors());
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
    	super(from.getAlgo());
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
        switch (getAlgo()) {
        default:
        case NEGAMAX:
            pool.invoke(new NegamaxAction<M>(this, wrapper, null, depth, -maxEvaluateValue(), maxEvaluateValue()));
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
		
		private double alpha, beta;

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
			if (alpha >= beta) {
				return null;
			}
            try {
                return negamax(wrapper, depth, alpha, beta);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return alpha;
		}
    	
	    private double negamax(final MoveWrapper<M> wrapper, final int depth, double alpha, double beta) throws InterruptedException, ExecutionException {
	        if (depth == 0 || minimax.isOver()) {
	            return minimax.evaluate();
	        }
	        Collection<M> moves = minimax.getPossibleMoves();
	        if (moves.isEmpty()) {
	        	minimax.next();
	        	double score = -negamax(null, depth - 1, -beta, -alpha);
	        	minimax.previous();
	        	return score;
	        } else {
	            boolean first = true;
	            Collection<NegamaxAction<M>> tasks = new LinkedList<>();
	            for (M move : moves) {
	                if (first || getPool().getQueuedTaskCount() > 0) {
	                    // young brother wait...
	                    // reduce alpha beta window
	                    // assume it's the best possible move
	                    first = false;
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
	                            break;
	                        }
	                    }
	                } else {
	                	// at least one worker free
	                	// let send some work to it !
	                	ParallelMinimax<M> clone = minimax.clone();
	                	clone.makeMove(move);
	                	NegamaxAction<M> task = new NegamaxAction<M>(clone, null, move, depth - 1, -beta, -alpha);
	                    task.fork();
	                    tasks.add(task);
	                }
	            }
	            // await termination of all brothers
	            // once all done alpha == best score
	            for (NegamaxAction<M> task : tasks) {
	                if (task.join() == null) { 
	                	// task cancelled due to a brother's cutoff
	                	// we don't need to wait for other brothers
	                	// other brothers will cancel automatically!
	                	break;
	                } else if (-task.getRawResult() <= alpha) {
	                	// not interesting
	                	continue;
	                } else {
	                	alpha = -task.getRawResult();
                        if (wrapper != null) {
                            wrapper.move = task.move;
                        }
                        if (alpha >= beta) {
    	                    // task lead to a cutoff...
    	                	// we don't need to wait for other brothers
    	                	// other brothers will cancel automatically!
                            break;
                        }
                	}
	            }
	            // sub tree done !
	            return alpha;
	        }
	    }
	    
    }
    
}
