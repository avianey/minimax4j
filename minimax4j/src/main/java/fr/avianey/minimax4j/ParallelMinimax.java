package fr.avianey.minimax4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

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
    
    // TODO
    // do it lighter with a threadgrou and n thread
    // or a fixed threadpool executor
    // let task be simple runnable or callable
    private static final ForkJoinPool pool = new ForkJoinPool(4);
    
    /**
     * Creates a new AI using the {@link Algorithm#NEGAMAX} algorithm 
     * with a parallelism given by {@link Runtime#availableProcessors()}
     */
    public ParallelMinimax() {
        this(Algorithm.NEGAMAX);
    }
    
    /**
     * Creates a new IA using the provided {@link Algorithm}
     * with a parallelism given by {@link Runtime#availableProcessors()}
     * @param algo The decision rule to use
     * @see Algorithm
     */
    public ParallelMinimax(Algorithm algo) {
        super(algo);
    }
    
    /**
     * Get the best {@link Move} for the given search depth using multiple threads<br/>
     * This methods iterates over {@link #getPossibleMoves()} to find the best one.
     * Work is dispatched across workers as long as the specified parallelism is not reach...
     * If two or more {@link Move} lead to the same best evaluation, parallelism does not insure that
     * the first one will be returned by this function. Depending of the size of sub-trees, the returned
     * move is the one that is first computed...
     * @param depth The search depth (must be > 0)
     * @return
     */
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        switch (getAlgo()) {
        default:
        case NEGAMAX:
        	NegamaxMasterTask<M> task = new NegamaxMasterTask<M>(this, wrapper, depth, new Window(-maxEvaluateValue(), maxEvaluateValue())); 
        	pool.invoke(task);
        	return wrapper.move;
        }
    }
    
    static abstract class RecursiveNegamaxAction<M extends Move> extends RecursiveAction {
    	
    	static final class SubTreeWaitingException extends Exception {
			private static final long serialVersionUID = 1L;
		}

		private static final long serialVersionUID = 1L;

		final ParallelMinimax<M> minimax;
		
		RecursiveNegamaxAction(final ParallelMinimax<M> minimax) {
			this.minimax = minimax;
		}
		
	    double negamax(final MoveWrapper<M> wrapper, final int depth, Window window) throws InterruptedException, ExecutionException {
	        if (depth == 0 || minimax.isOver()) {
	            return minimax.evaluate();
	        }
	        Collection<M> moves = minimax.getPossibleMoves();
	        if (moves.isEmpty()) {
	        	minimax.next();
	        	double score = negamaxScore(depth, window);
	        	minimax.previous();
	        	return score;
	        } else {
	            boolean first = true;
	            double score;
	            Collection<ForkJoinTask<?>> tasks = new LinkedList<>();
	            for (M move : moves) {
	                if (first || pool.getQueuedTaskCount() > 0) {
	                    // young brother wait...
	                	// or all workers running (no need to fork then)
	                    first = false;
	                    minimax.makeMove(move);
	                    score = negamaxScore(depth, window);
	                    minimax.unmakeMove(move);
	                    if (score > window.alpha) {
	                    	synchronized (window) {
	                    		if (score > window.alpha) {
			                    	window.alpha = score;
			                        if (wrapper != null) {
			                            wrapper.move = move;
			                        }
			                        if (window.alpha >= window.beta) {
			                            break;
			                        }
	                    		}
	                    	}
	                    }
	                } else {
	                	// fork sub-tree exploration
	                    NegamaxTask<M> task = new NegamaxTask<>(minimax.clone(), wrapper, move, depth, window);
	                    task.fork();
	                    tasks.add(task);
	                }
	            }
	            // await termination of all brothers
	            // this will bloc the current worker
	            // ... need to be optimized !
	            for (ForkJoinTask<?> task : tasks) {
	                while (!task.isDone()) {
	                    // waiting for the task to complete
	                    // don't let the current thread idle!
	                    // TODO
	                    // if child task not started, run it there
	                    // if child started pick a runnable
	                    // the picked runnable must be of ply > current depth
	                }
	            }
	            return window.alpha;
	        }
	    }

	    double negamaxScore(final int depth, Window window) throws InterruptedException, ExecutionException {
			return -negamax(null, depth - 1, window.inverse());
		}
		
    }
     
    static class NegamaxMasterTask<M extends Move> extends NegamaxTask<M> {

        private static final long serialVersionUID = 1L;
        
        public NegamaxMasterTask(ParallelMinimax<M> minimax, MoveWrapper<M> wrapper, int depth, Window window) {
        	super(minimax, wrapper, null, depth, window);
        }
        
        @Override
        protected void compute() {
            try {
				negamax(wrapper, depth, window);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
        }
        
    }
    
    static class NegamaxTask<M extends Move> extends RecursiveNegamaxAction<M> {

        private static final long serialVersionUID = 1L;
        
        final int depth;
        final Window window;
        final MoveWrapper<M> wrapper;
        final M move;
        
        NegamaxTask(ParallelMinimax<M> minimax, MoveWrapper<M> wrapper, M move, int depth, Window window) {
            super(minimax);
        	this.depth = depth;
            this.window = window;
            this.wrapper = wrapper;
            this.move = move;
        }
        
        @Override
        protected void compute() {
            if (window.alpha >= window.beta) {
            	// abort if a cutoff has been found
            	// before the execution of this task
                return;
            }
        	Double score = window.alpha;
        	minimax.makeMove(move);
			try {
				score = negamaxScore(depth, window);
			} catch (InterruptedException | ExecutionException e) {}
            if (score > window.alpha) {
            	synchronized (window) {
            		if (score > window.alpha) {
		            	window.alpha = score;
		                if (wrapper != null) {
		                    wrapper.move = move;
		                }
            		}
            	}
            }
        }
    }
    
    static class Window {
    	public volatile double alpha;
    	public volatile double beta;
		public Window(double alpha, double beta) {
			this.alpha = alpha;
			this.beta = beta;
		}
		public Window inverse() {
			return new Window(-beta, -alpha);
		}
    }
    
    @Override
    public abstract ParallelMinimax<M> clone();
    
    public void release(ParallelMinimax<M> minimax) {}
}
