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

import java.util.AbstractList;

class TranspositionStateWithSymetricTransposition extends TranspositionState {

    private final ZobristHashing hash90;
    private final ZobristHashing hash180;
    private final ZobristHashing hash270;

    private final Iterable<Integer> symetricTranspositions = new SymetricTranspositionsList();

    TranspositionStateWithSymetricTransposition() {
        super();
        hash90 = new ZobristHashing(hash);
        hash180 = new ZobristHashing(hash);
        hash270 = new ZobristHashing(hash);
    }

    @Override
    public void clean() {
        super.clean();
        hash90.reset();
        hash180.reset();
        hash270.reset();
    }

    Integer getTranspositionValue() {
        return hash.hashCode();
    }

    Integer getGroup() {
        return turn;
    }

    public Iterable<Integer> getSymetricTranspositionValues() {
        return symetricTranspositions;
    }

    @Override
    void makeMove(IAMove move) {
        hash.add(currentPlayer, move.getPosition());
        super.makeMove(move);
    }

    @Override
    void unmakeMove(IAMove move) {
        super.unmakeMove(move);
        hash.add(currentPlayer, move.getPosition());
    }

    private class SymetricTranspositionsList extends AbstractList<Integer> {
        @Override
        public Integer get(int i) {
            switch (i) {
                case 0:
                    return hash.hashCode();
                case 1:
                    return hash90.hashCode();
                case 2:
                    return hash180.hashCode();
                case 3:
                    return hash270.hashCode();
            }
            return 0;
        }
        @Override
        public int size() {
            return 4;
        }
    }
}
