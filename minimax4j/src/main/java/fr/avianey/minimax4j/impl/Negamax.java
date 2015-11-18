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
 * Negamax based implementation.
 *
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
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class Negamax<M extends Move> implements IA<M> {

    @Override
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        negamax(wrapper, depth, -maxEvaluateValue(), maxEvaluateValue());
        return wrapper.move;
    }

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
            double score;
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

}
