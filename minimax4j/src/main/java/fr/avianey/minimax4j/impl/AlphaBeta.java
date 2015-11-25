/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * The MIT License (MIT)

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

import java.util.Collection;

/**
 * AlphaBeta based implementation.
 *
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
 *
 * Initial call for maximizing player
 * <pre>alphabeta(origin, depth, -8, +8, TRUE)</pre>
 *
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public abstract class AlphaBeta<M extends Move> implements IA<M> {

    @Override
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        alphabeta(wrapper, depth, 1, -maxEvaluateValue(), maxEvaluateValue());
        return wrapper.move;
    }

    private double alphabeta(final MoveWrapper<M> wrapper, final int depth, final int who, double alpha, double beta) {
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

}
