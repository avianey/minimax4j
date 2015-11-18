/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *
 * The MIT License (MIT)
 *
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
package fr.avianey.minimax4j.ia;

import fr.avianey.minimax4j.impl.ParallelNegamax;

import java.util.List;

/**
 * Parallel version of the test {@code BasicMinimax}.
 *
 * @author antoine vianey
 */
public class ParallelIA extends ParallelNegamax<IAMove> implements Cleanable {

    private final Logic logic;
    private final State state;

    public ParallelIA(int nbPlayers) {
        logic = new Logic(nbPlayers);
        state = new State(nbPlayers);
    }

    private ParallelIA(ParallelIA ia) {
        super(ia);
        logic = ia.logic;
        state = ia.state.clone();
    }

    @Override
    public void clean() {
        state.clean();
    }

    @Override
    public ParallelNegamax<IAMove> clone() {
        return new ParallelIA(this);
    }

    @Override
    public boolean isOver() {
        return logic.isOver(state);
    }

    @Override
    public void makeMove(IAMove move) {
        state.makeMove(move);
    }

    @Override
    public void unmakeMove(IAMove move) {
        state.unmakeMove(move);
    }

    @Override
    public List<IAMove> getPossibleMoves() {
        return logic.getPossibleMoves(state);
    }

    @Override
    public double evaluate() {
        return logic.evaluate(state);
    }

    @Override
    public double maxEvaluateValue() {
        return logic.maxEvaluateValue();
    }

    @Override
    public void next() {
        state.next();
    }

    @Override
    public void previous() {
        state.previous();
    }

}
