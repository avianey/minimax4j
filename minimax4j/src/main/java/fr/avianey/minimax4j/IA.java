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

/**
 * Abstract class implementing minimax and derivated decision rules for two-person 
 * <a href="http://en.wikipedia.org/wiki/Zero-sum_game">zero-sum</a> games of perfect information.
 * Extend this class to implement IA for several games, such as :
 * <ul>
 * <li>Chess</li>
 * <li>Reversi</li>
 * <li>Checkers</li>
 * <li>Go</li>
 * <li>Connect Four</li>
 * <li>Tic Tac Toe</li>
 * <li>...</li>
 * </ul>
 * 
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public interface IA<M extends Move> {
    
    /**
     * Available decision rules
     * 
     * @author antoine vianey
     */
    enum Algorithm {
        /** 
         * The IA algorithm (slowest)
         * @see http://en.wikipedia.org/wiki/IA
         **/
        MINIMAX,
        /** 
         * The Mininma algorithm with alpha-beta pruning 
         * @see http://en.wikipedia.org/wiki/Alpha-beta_pruning
         **/
        ALPHA_BETA,
        /** 
         * The Negamax algorithm with alpha-beta pruning
         * @see http://en.wikipedia.org/wiki/Negamax
         **/
        NEGAMAX,
        /** 
         * The Negascout algorithm (fastest when strong move ordering is provided)<br/>
         * Also called Principal Variation Search...
         * @see IA#getPossibleMoves()
         * @see http://en.wikipedia.org/wiki/Negascout
         **/
        NEGASCOUT;
    }
    
    /**
     * Get the best {@link Move} for the given search depth<br/>
     * This methods iterates over {@link #getPossibleMoves()} to find the best one.
     * If two or more {@link Move} lead to the same best evaluation, the first one is returned.
     * @param depth The search depth (must be > 0)
     * @return the best possible move for the {@link #evaluate()} function
     */
    M getBestMove(final int depth);
    
    /**
     * Tell weather or not the game is over.
     * @return
     *         True if the game is over
     */
    boolean isOver();
    
    /**
     * Play the given move and modify the state of the game.<br/>
     * This function <strong>MUST</strong> set correctly the turn of the next player
     * ... by calling the next() method for example.
     * @param move
     *             The move to play
     * @see #next()
     */
    void makeMove(M move);
    
    /**
     * Undo the given move and restore the state of the game.<br/>
     * This function <strong>MUST</strong> restore correctly the turn of the previous player
     * ... by calling the previous() method for example.
     * @param move
     *             The move to cancel
     * @see #previous()
     */
    void unmakeMove(M move);
    
    /**
     * List every valid moves for the current player.<br><br>
     * <i>"Improvement (of the alpha beta pruning) can be achieved without 
     * sacrificing accuracy, by using ordering heuristics to search parts 
     * of the tree that are likely to force alpha-beta cutoffs early."</i>
     * <br>- <a href="http://en.wikipedia.org/wiki/Alpha-beta_pruning#Heuristic_improvements">Alpha-beta pruning on Wikipedia</a>
     * @return
     *         The list of the current player possible moves
     */
    List<M> getPossibleMoves();

    /**
     * Evaluate the state of the game <strong>for the current player</strong> after a move.
     * The greatest the value is, the better the position of the current player is.
     * @return
     *         The evaluation of the position for the current player
     * @see #maxEvaluateValue()
     */
    double evaluate();
    
    /**
     * The absolute maximal value for the evaluate function.
     * This value must not be equal to a possible return value of the evaluation function.
     * @return
     *         The <strong>non inclusive</strong> maximal value
     * @see #evaluate()
     */
    double maxEvaluateValue();
    
    /**
     * Change current turn to the next player.
     * This method must not be used in conjunction with the makeMove() method.
     * Use it to implement a <strong>pass</strong> functionality.
     * @see #makeMove(Move)
     */
    void next();
    
    /**
     * Change current turn to the previous player.
     * This method must not be used in conjunction with the unmakeMove() method.
     * Use it to implement an <strong>undo</strong> functionality.
     * @see #unmakeMove(Move)
     */
    void previous();

}
