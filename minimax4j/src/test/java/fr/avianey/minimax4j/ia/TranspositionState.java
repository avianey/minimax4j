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

import fr.avianey.bitboard4j.hash.ZobristHashing;

import java.util.Arrays;

import static fr.avianey.minimax4j.ia.Logic.GRID_SIZE;

class TranspositionState extends BaseState {

    protected final ZobristHashing hash;

    TranspositionState() {
        super();
        hash = new ZobristHashing(2, GRID_SIZE);
    }

    @Override
    public void clean() {
        super.clean();
        hash.reset();
    }

    Integer getTranspositionValue() {
        return hash.hashCode();
    }

    Integer getGroup() {
        return turn;
    }

    @Override
    void makeMove(IAMove move) {
        hash.add(currentPlayer, move.getPosition());
        super.makeMove(move);
    }

    @Override
    void unmakeMove(IAMove move) {
        super.unmakeMove(move);
        hash.remove(currentPlayer, move.getPosition());
    }

}
