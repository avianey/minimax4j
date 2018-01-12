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
package fr.avianey.minimax4j;

import java.util.List;

public abstract class IADecorator<M extends Move> implements IA<M> {

    protected final IA<M> ia;

    public IADecorator(IA<M> ia) {
        this.ia = ia;
    }

    @Override
    public List<M> getBestMoves(int depth) {
        return ia.getBestMoves(depth);
    }

    @Override
    public boolean isOver() {
        return ia.isOver();
    }

    @Override
    public void makeMove(M move) {
        ia.makeMove(move);
    }

    @Override
    public void unmakeMove(M move) {
        ia.unmakeMove(move);
    }

    @Override
    public Iterable<M> getPossibleMoves() {
        return ia.getPossibleMoves();
    }

    @Override
    public double evaluate() {
        return ia.evaluate();
    }

    @Override
    public double maxEvaluateValue() {
        return ia.maxEvaluateValue();
    }

    @Override
    public void next() {
        ia.next();
    }

    @Override
    public void previous() {
        ia.previous();
    }
}
