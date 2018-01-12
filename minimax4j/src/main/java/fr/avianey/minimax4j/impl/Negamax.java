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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static fr.avianey.minimax4j.IAUtils.iterableToSortedList;

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

    public List<M> getBestMoves(final int depth, Iterable<M> possibleMoves) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        List<M> orderedMoves = iterableToSortedList(possibleMoves);
        negamax(orderedMoves, depth, -maxEvaluateValue(), maxEvaluateValue());
        Collections.sort(orderedMoves);
        return orderedMoves;
    }

    protected double negamax(Iterable<M> initialMoves, final int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        Iterator<M> moves = (initialMoves != null ? initialMoves : getPossibleMoves()).iterator();
        if (!moves.hasNext()) {
        	next();
        	double score = -negamax(null, depth - 1, -beta, -alpha);
        	previous();
        	return score;
        }
        double score;
        while (moves.hasNext()) {
            M move = moves.next();
            makeMove(move);
            score = -negamax(null, depth - 1, -beta, -alpha);
            unmakeMove(move);
            if (initialMoves != null) {
                move.value = score;
            }
            if (score > alpha) {
                alpha = score;
                if (alpha >= beta) {
                    break;
                }
            }
        }
        return alpha;
    }

}
