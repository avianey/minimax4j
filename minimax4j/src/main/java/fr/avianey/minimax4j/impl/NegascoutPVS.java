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

import java.util.List;

/**
 * Negascout PVS implementation.
 *
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
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class NegascoutPVS<M extends Move> implements IA<M> {

    @Override
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        negascout(wrapper, depth, -maxEvaluateValue(), maxEvaluateValue());
        return wrapper.move;
    }

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

}
