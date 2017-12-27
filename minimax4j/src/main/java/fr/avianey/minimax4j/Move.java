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
package fr.avianey.minimax4j;

import static java.lang.Double.isNaN;
import static java.lang.Math.signum;

/**
 * 
 * Implement this interface to describe a Move in your game.<br>
 * A typical implementation for a Chess game would be :
 * <ul>
 * <li>The color of the piece</li>
 * <li>The type of the piece (king, queen, pawn, ...)</li>
 * <li>The position before the move</li>
 * <li>The position after the move</li>
 * </ul>
 * Additional information might be necessary to implement the abstract {@link IA#unmakeMove(Move)} method of the {@link IA} class :
 * <ul>
 * <li>Taken pieces</li>
 * <li>...</li>
 * </ul>
 * 
 * @author antoine vianey
 * @see IA#unmakeMove(Move)
 * @see IA#makeMove(Move)
 *
 */
public abstract class Move implements Comparable<Move> {

    public double value = Double.NaN;

    @Override
    public int compareTo(Move move) {
        if (isNaN(value) && isNaN(move.value)) {
            return 0;
        } else if (isNaN(value)) {
            return -1;
        } else if (isNaN(move.value)) {
            return 1;
        }
        return (int) signum(move.value - value);
    }

}
