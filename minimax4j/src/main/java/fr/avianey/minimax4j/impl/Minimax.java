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
 * Minimax based implementation.
 *
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
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 * @deprecated For testing and documentation purpose only, the preferred way to implement IA is to use {@link Negamax}.
 */
@Deprecated
public abstract class Minimax<M extends Move> implements IA<M> {

    @Override
    public List<M> getBestMoves(final int depth, Iterable<M> possibleMoves) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        List<M> orderedMoves = iterableToSortedList(possibleMoves);
        minimax(orderedMoves, depth, 1);
        Collections.sort(orderedMoves);
        return orderedMoves;
    }

    private double minimax(Iterable<M> initialMoves, final int depth, final int who) {
        if (depth == 0 || isOver()) {
            return who * evaluate();
        }
        Iterator<M> moves = (initialMoves != null ? initialMoves : getPossibleMoves()).iterator();
        if (!moves.hasNext()) {
        	next();
            double score = minimaxScore(depth, who);
            previous();
            return score;
        }
        if (who > 0) {
            // max
            double score;
            double bestScore = -maxEvaluateValue();
            while (moves.hasNext()) {
                M move = moves.next();
                makeMove(move);
                score = minimaxScore(depth, who);
                unmakeMove(move);
                if (initialMoves != null) {
                    move.value = score;
                }
                if (score > bestScore) {
                    bestScore = score;
                }
            }
            return bestScore;
        } else {
            // min
            double score;
            double bestScore = maxEvaluateValue();
            while (moves.hasNext()) {
                M move = moves.next();
                makeMove(move);
                score = minimaxScore(depth, who);
                unmakeMove(move);
                if (initialMoves != null) {
                    move.value = score;
                }
                if (score < bestScore) {
                    bestScore = score;
                }
            }
            return bestScore;
        }
    }
    
    protected double minimaxScore(final int depth, final int who) {
		return minimax(null, depth - 1, -who);
	}

}
