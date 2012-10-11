package fr.pixelprose.minimax4j;

import java.util.Collection;
import java.util.List;

/*
 *  This file is part of minimax4j.
 *  <https://github.com/avianey/minimax4j>
 *  
 *  Copyright (C) 2012 Antoine Vianey
 *  
 *  minimax4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  minimax4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with minimax4j. If not, see <http://www.gnu.org/licenses/>
 */

/**
 * Abstract class implementing minimax and derivated decision rules for two-person zero-sum games of perfect information.
 * This class can be use to develop IA for several games, such as :
 * <ul>
 * <li>Chess</li>
 * <li>Reversi</li>
 * <li>Checkers</li>
 * <li>Go</li>
 * <li>Four-in-a-row</li>
 * <li>Tica Tac Toe</li>
 * <li>...</li>
 * </ul>
 * 
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class IA<M extends Move> {
    
    private final Algorithm algo;
    
    private final class IAMoveWrapper {
        M move;
    }
    
    /**
     * Available decision rules
     * 
     * @author antoine vianey
     */
    public static enum Algorithm {
        /** The Minimax algorithm (slowest) */
        MINIMAX,
        /** The Mininma algorithm with alpha-beta pruning */
        ALPHA_BETA,
        /** The Negamax algorithm with alpha-beta pruning */
        NEGAMAX,
        /** The Negascout algorithm (fastest) */
        NEGASCOUT;
    }
    
    /**
     * Creates a new IA using the Negamax algorithm
     * Negascout performs slowly on several tests for the moment...
     */
    public IA() {
        this(Algorithm.NEGAMAX);
    }
    
    /**
     * Creates a new IA using the provided algorithm
     * @param algo
     *             The decision rule to use
     * @see Algorithm
     */
    public IA(Algorithm algo) {
        this.algo = algo;
    }
    
    public M getBestMove() {
        IAMoveWrapper wrapper = new IAMoveWrapper();
        switch (algo) {
        case MINIMAX:
            minimax(wrapper, 0, getDifficulty().getDepth());
            break;
        case ALPHA_BETA:
            alphabeta(wrapper, 0, getDifficulty().getDepth(), -maxEvaluateValue(), maxEvaluateValue());
            break;
        case NEGAMAX:
            negamax(wrapper, getDifficulty().getDepth(), -maxEvaluateValue(), maxEvaluateValue());
            break;
        case NEGASCOUT:
        default:
            negascout(wrapper, getDifficulty().getDepth(), -maxEvaluateValue(), maxEvaluateValue());
            break;
        }
        return wrapper.move;
    }
    
    private final double minimax(final IAMoveWrapper wrapper, int depth, int DEPTH) {
        if (depth == DEPTH) {
            return evaluate();
        } else if (isOver()) {
            // if depth not reach, must consider who's playing
            return (((DEPTH - depth) % 2) == 1 ? -1 : 1) * evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return minimax(null, depth + 1, DEPTH);
        }
        if (depth % 2 == DEPTH % 2) {
            double score = -maxEvaluateValue();
            double bestScore = -maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                score = minimax(null, depth + 1, DEPTH);
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
                score = minimax(null, depth + 1, DEPTH);
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
    
    private final double alphabeta(final IAMoveWrapper wrapper, int depth, int DEPTH, double alpha, double beta) {
        if (depth == DEPTH) {
            return evaluate();
        } else if (isOver()) {
            // if depth not reach, must consider who's playing
            return (((DEPTH - depth) % 2) == 1 ? -1 : 1) * evaluate();
        }
        M bestMove = null;
        double score;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return alphabeta(null, depth + 1, DEPTH, alpha, beta);
        }
        if (depth % 2 == DEPTH % 2) {
            for (M move : moves) {
                makeMove(move);
                score = alphabeta(null, depth + 1, DEPTH, alpha, beta);
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
                score = alphabeta(null, depth + 1, DEPTH, alpha, beta);
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
    
    private final double negamax(final IAMoveWrapper wrapper, int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return -negamax(null, depth - 1, -beta, -alpha);
        } else {
            double score = -maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                score = -negamax(null, depth - 1, -beta, -alpha);
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
    
    private final double negascout(IAMoveWrapper wrapper, int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        List<M> moves = getPossibleMoves();
        double b = beta;
        M bestMove = null;
        if (moves.isEmpty()) {
            return -negascout(null, depth - 1, -beta, -alpha);
        } else {
            double score;
            boolean first = true;
            for (M move : moves) {
                makeMove(move);
                score = -negascout(null, depth - 1, -b, -alpha);
                if (!first && alpha < score && score < beta) {
                    score = -negascout(null, depth - 1, -beta, -alpha);
                }
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
    
    /**
     * Get the IA difficulty level for the current player
     * @return
     *         The difficulty
     */
    public abstract Difficulty getDifficulty();
    
    /**
     * Tell weather or not the game is over.
     * @return
     *         True if the game is over
     */
    public abstract boolean isOver();
    
    /**
     * Play the given move and modify the state of the game.
     * This function must set correctly the turn of the next player
     * ... by calling the next() method for example.
     * @param move
     *             The move to play
     * @see #next()
     */
    public abstract void makeMove(M move);
    
    /**
     * Undo the given move and restore the state of the game.
     * This function must restore correctly the turn of the previous player
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
     * @see #minEvaluateValue()
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
    
    
}
