package fr.avianey.minimax4j;

import java.util.Collection;
import java.util.List;

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
 * Abstract class implementing minimax and derivated decision rules for two-person 
 * <a href="http://en.wikipedia.org/wiki/Zero-sum_game">zero-sum</a> games of perfect information.
 * Extend this class to implement IA for several games, such as :
 * <ul>
 * <li>Chess</li>
 * <li>Reversi</li>
 * <li>Checkers</li>
 * <li>Go</li>
 * <li>Connect Four</li>
 * <li>Tic Tac Toe</li>
 * <li>...</li>
 * </ul>
 * 
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class Minimax<M extends Move> {
    
    private final Algorithm algo;
    
    static final class MoveWrapper<M extends Move> {
        public M move;
    }
    
    /**
     * Available decision rules
     * 
     * @author antoine vianey
     */
    public static enum Algorithm {
        /** 
         * The Minimax algorithm (slowest) 
         * @see http://en.wikipedia.org/wiki/Minimax
         **/
        MINIMAX,
        /** 
         * The Mininma algorithm with alpha-beta pruning 
         * @see http://en.wikipedia.org/wiki/Alpha-beta_pruning
         **/
        ALPHA_BETA,
        /** 
         * The Negamax algorithm with alpha-beta pruning
         * @see http://en.wikipedia.org/wiki/Negamax
         **/
        NEGAMAX,
        /** 
         * The Negascout algorithm (fastest when strong move ordering is provided)<br/>
         * Also called Principal Variation Search...
         * @see Minimax#getPossibleMoves()
         * @see http://en.wikipedia.org/wiki/Negascout
         **/
        NEGASCOUT;
    }
    
    /**
     * Creates a new IA using the {@link Algorithm#NEGAMAX} algorithm<br/>
     * {@link Algorithm#NEGASCOUT} performs slowly in case of a weak move ordering...
     */
    public Minimax() {
        this(Algorithm.NEGAMAX);
    }
    
    /**
     * Creates a new IA using the provided algorithm
     * @param algo The decision rule to use
     * @see Algorithm
     */
    public Minimax(Algorithm algo) {
        this.algo = algo;
    }
    
    /**
     * Get the best {@link Move} for the given search depth<br/>
     * This methods iterates over {@link #getPossibleMoves()} to find the best one.
     * If two or more {@link Move} lead to the same best evaluation, the first one is returned.
     * @param depth The search depth (must be > 0)
     * @return
     */
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        switch (algo) {
        case MINIMAX:
            minimax(wrapper, depth, 1);
            break;
        case ALPHA_BETA:
            alphabeta(wrapper, depth, 1, -maxEvaluateValue(), maxEvaluateValue());
            break;
        case NEGAMAX:
            negamax(wrapper, depth, -maxEvaluateValue(), maxEvaluateValue());
            break;
        case NEGASCOUT:
        default:
            negascout(wrapper, depth, -maxEvaluateValue(), maxEvaluateValue());
            break;
        }
        return wrapper.move;
    }
    
    /**
     * Minimax algorithm implementation :
     * <pre>
     * function minimax(node, depth, maximizingPlayer)
     *     if depth = 0 or node is a terminal node
     *         return the heuristic value of node
     *     if maximizingPlayer
     *         bestValue := -&#8734;
     *         for each child of node
     *             val := minimax(child, depth - 1, FALSE)
     *             bestValue := max(bestValue, val)
     *         return bestValue
     *     else
     *         bestValue := +&#8734;
     *         for each child of node
     *             val := minimax(child, depth - 1, TRUE)
     *             bestValue := min(bestValue, val)
     *         return bestValue
     * </pre>
     * 
     * Initial call for maximizing player
     * <pre>minimax(origin, depth, TRUE)</pre>
     * 
     * @param wrapper
     * @param depth
     * @param DEPTH
     * @return
     */
    private final double minimax(final MoveWrapper<M> wrapper, final int depth, final int who) {
        if (depth == 0 || isOver()) {
            return who * evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
        	next();
            double score = minimaxScore(depth, who);
            previous();
            return score;
        }
        if (who > 0) {
            double score = -maxEvaluateValue();
            double bestScore = -maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                score = minimaxScore(depth, who);
                unmakeMove(move);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return bestScore;
        } else {
            double score = maxEvaluateValue();
            double bestScore = maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                score = minimaxScore(depth, who);
                unmakeMove(move);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return bestScore;
        }
    }
    
    protected double minimaxScore(final int depth, final int who) {
		return minimax(null, depth - 1, -who);
	}

	/**
     * Minimax with alpha beta algorithm :
     * <pre>
     * function alphabeta(node, depth, &#945;, &#946;, maximizingPlayer)
	 *     if depth = 0 or node is a terminal node
	 *         return the heuristic value of node
	 *     if maximizingPlayer
	 *         for each child of node
	 *             &#945; := max(&#945;, alphabeta(child, depth - 1, &#945;, &#946;, FALSE))
	 *             if &#946; <= &#945;
	 *                 break (* &#946; cut-off *)
	 *         return a
	 *     else
	 *         for each child of node
	 *             &#946; := min(&#946;, alphabeta(child, depth - 1, &#945;, &#946;, TRUE))
	 *             if &#946; <= &#945;
	 *                 break (* &#945; cut-off *)
	 *         return &#946;
	 * </pre>
	 * Initial call for maximizing player
     * <pre>alphabeta(origin, depth, -8, +8, TRUE)</pre>
     * 
     * @param wrapper
     * @param depth
     * @param who
     * @param alpha
     * @param beta
     * @return
     */
    private final double alphabeta(final MoveWrapper<M> wrapper, final int depth, final int who, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return who * evaluate();
        }
        M bestMove = null;
        double score;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
        	next();
            score = alphabetaScore(depth, who, alpha, beta);
            previous();
            return score;
        }
        if (who > 0) {
            for (M move : moves) {
                makeMove(move);
                score = alphabetaScore(depth, who, alpha, beta);
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        } else {
            for (M move : moves) {
                makeMove(move);
                score = alphabetaScore(depth, who, alpha, beta);
                unmakeMove(move);
                if (score < beta) {
                    beta = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return beta;
        }
    }

    protected double alphabetaScore(final int depth, final int who, final double alpha, final double beta) {
		return alphabeta(null, depth - 1, -who, alpha, beta);
	}
    
    /**
     * Negamax algorithm :
     * <pre>
     * function negamax(node, depth, color)
     *     if depth = 0 or node is a terminal node
     *         return color * the heuristic value of node
     *     bestValue := -&#8734;
     *     foreach child of node
     *         val := -negamax(child, depth - 1, -color)
     *         bestValue := max( bestValue, val )
     *     return bestValue
     * </pre>
     * 
     * Initial call for Player A's root node
     * <pre>
     * rootNegamaxValue := negamax( rootNode, depth, 1)
     * rootMinimaxValue := rootNegamaxValue
     * </pre>
     * 
     * Initial call for Player B's root node
     * <pre>
     * rootNegamaxValue := negamax( rootNode, depth, -1)
     * rootMinimaxValue := -rootNegamaxValue
     * </pre>
     * 
     * This implementation use alpha-beta cut-offs.
     * 
     * @param wrapper
     * @param depth
     * @param alpha
     * @param beta
     * @return
     */
    private double negamax(final MoveWrapper<M> wrapper, final int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
        	next();
        	double score = negamaxScore(depth, alpha, beta);
        	previous();
        	return score;
        } else {
            double score = -maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                score = negamaxScore(depth, alpha, beta);
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        }
    }

    protected double negamaxScore(final int depth, final double alpha, final double beta) {
		return -negamax(null, depth - 1, -beta, -alpha);
	}
    
    /**
     * Negascout PVS algorithm :
     * <pre>
     * function pvs(node, depth, &#945;, &#946;, color)
     *     if node is a terminal node or depth = 0
     *         return color x the heuristic value of node
     *     for each child of node
     *         if child is not first child
     *             score := -pvs(child, depth-1, -&#945;-1, -&#945;, -color)       (* search with a null window *)
     *             if &#945; < score < &#946;                                      (* if it failed high,
     *                 score := -pvs(child, depth-1, -&#946;, -score, -color)         do a full re-search *)
     *         else
     *             score := -pvs(child, depth-1, -&#946;, -&#945;, -color)
     *         &#945; := max(&#945;, score)
     *         if &#945; >= &#946;
     *             break                                            (* beta cut-off *)
     *     return &#945;
     * </pre>
     * 
     * @param wrapper
     * @param depth
     * @param alpha
     * @param beta
     * @return
     */
    private double negascout(final MoveWrapper<M> wrapper, final int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        List<M> moves = getPossibleMoves();
        double b = beta;
        M bestMove = null;
        if (moves.isEmpty()) {
        	next();
            double score = negascoutScore(true, depth, alpha, beta, b);
            previous();
            return score;
        } else {
            double score;
            boolean first = true;
            for (M move : moves) {
                makeMove(move);
                score = negascoutScore(first, depth, alpha, beta, b);
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
                b = alpha + 1;
                first = false;
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        }
    }

    protected double negascoutScore(final boolean first, final int depth, final double alpha, final double beta, final double b) {
    	double score = -negascout(null, depth - 1, -b, -alpha);
        if (!first && alpha < score && score < beta) {
            // fails high... full re-search
            score = -negascout(null, depth - 1, -beta, -alpha);
        }
        return score;
	}
    
    /**
     * Tell weather or not the game is over.
     * @return
     *         True if the game is over
     */
    public abstract boolean isOver();
    
    /**
     * Play the given move and modify the state of the game.<br/>
     * This function <strong>MUST</strong> set correctly the turn of the next player
     * ... by calling the next() method for example.
     * @param move
     *             The move to play
     * @see #next()
     */
    public abstract void makeMove(M move);
    
    /**
     * Undo the given move and restore the state of the game.<br/>
     * This function <strong>MUST</strong> restore correctly the turn of the previous player
     * ... by calling the previous() method for example.
     * @param move
     *             The move to cancel
     * @see #previous()
     */
    public abstract void unmakeMove(M move);
    
    /**
     * List every valid moves for the current player.<br><br>
     * <i>"Improvement (of the alpha beta pruning) can be achieved without 
     * sacrificing accuracy, by using ordering heuristics to search parts 
     * of the tree that are likely to force alpha-beta cutoffs early."</i>
     * <br>- <a href="http://en.wikipedia.org/wiki/Alpha-beta_pruning#Heuristic_improvements">Alpha-beta pruning on Wikipedia</a>
     * @return
     *         The list of the current player possible moves
     */
    public abstract List<M> getPossibleMoves();

    /**
     * Evaluate the state of the game <strong>for the current player</strong> after a move.
     * The greatest the value is, the better the position of the current player is.
     * @return
     *         The evaluation of the position for the current player
     * @see #maxEvaluateValue()
     */
    public abstract double evaluate();
    
    /**
     * The absolute maximal value for the evaluate function.
     * This value must not be equal to a possible return value of the evaluation function.
     * @return
     *         The <strong>non inclusive</strong> maximal value
     * @see #evaluate()
     */
    public abstract double maxEvaluateValue();
    
    /**
     * Change current turn to the next player.
     * This method must not be used in conjunction with the makeMove() method.
     * Use it to implement a <strong>pass</strong> functionality.
     * @see #makeMove(Move)
     */
    public abstract void next();
    
    /**
     * Change current turn to the previous player.
     * This method must not be used in conjunction with the unmakeMove() method.
     * Use it to implement an <strong>undo</strong> functionality.
     * @see #unmakeMove(Move)
     */
    public abstract void previous();

    /**
     * Get the implementation used for tree search.
     * @return the {@link Algorithm} used.
     */
    public Algorithm getAlgo() {
        return algo;
    }
    
}
