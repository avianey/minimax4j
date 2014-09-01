package fr.avianey.minamax4j.sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.avianey.minimax4j.Minimax;
import fr.avianey.minimax4j.Minimax.Algorithm;
import fr.avianey.minimax4j.Move;
import fr.avianey.minimax4j.ParallelMinimax;
import fr.avianey.minimax4j.TranspositionMinimax;
import fr.avianey.minimax4j.sample.SampleRunner;
import fr.avianey.minimax4j.sample.SampleRunner.Listener;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeMinimax;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeMove;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeTranspositionMinimax;

/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012, 2013, 2014 Antoine Vianey
 * 
 * minimax4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with minimax4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */

/**
 * Check for every sample game that the same game is played exactly the same for each Algorithm :<br/>
 * <ul>
 * <li>Minimax</li>
 * <li>Alpha-beta</li>
 * <li>Negamax</li>
 * <li>Negascout</li>
 * </ul>
 * {@link Minimax} and {@link TranspositionMinimax} only are tested.<br/>
 * {@link ParallelMinimax} does not ensure that {@link ParallelMinimax#getBestMove(int)} returns the 
 * first {@link Move} in  the {@link ParallelMinimax#getPossibleMoves()} list that have the highest 
 * possible evaluated value (just like the serial {@link Minimax} does).
 * 
 * @author antoine vianey
 * @see Minimax
 * @see TranspositionMinimax
 * @see Minimax.Algorithm
 */
@RunWith(Parameterized.class)
public class Algorithms {
	
	private int depth;
	
	public Algorithms(int depth) {
		this.depth = depth;
	}
	
	@Parameters
	public static Collection<Object[]> params() {
	    return Arrays.asList(
	            new Object[] {1},
	            new Object[] {2},
	            new Object[] {3},
	            new Object[] {4},
	            new Object[] {5},
	            new Object[] {6},
	            new Object[] {7},
	            new Object[] {8},
	            new Object[] {9}
	        );
	}

	/**
	 * Tests that each {@link Algorithm} plays the same way
	 */
	@Test
    public void testTicTacToe() {
		// normal minimax
        SampleRunner<TicTacToeMove> minimax = new SampleRunner<TicTacToeMove>(new TicTacToeMinimax(Algorithm.MINIMAX), depth) {};
        SampleRunner<TicTacToeMove> alphabeta = new SampleRunner<TicTacToeMove>(new TicTacToeMinimax(Algorithm.ALPHA_BETA), depth) {};
        SampleRunner<TicTacToeMove> negamax = new SampleRunner<TicTacToeMove>(new TicTacToeMinimax(Algorithm.NEGAMAX), depth) {};
        SampleRunner<TicTacToeMove> negascout = new SampleRunner<TicTacToeMove>(new TicTacToeMinimax(Algorithm.NEGASCOUT), depth) {};
        // transposition Table backed minimax
        SampleRunner<TicTacToeMove> minimaxTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionMinimax(Algorithm.MINIMAX), depth) {};
        SampleRunner<TicTacToeMove> alphabetaTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionMinimax(Algorithm.ALPHA_BETA), depth) {};
        SampleRunner<TicTacToeMove> negamaxTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionMinimax(Algorithm.NEGAMAX), depth) {};
        SampleRunner<TicTacToeMove> negascoutTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionMinimax(Algorithm.NEGASCOUT), depth) {};
        
        final List<TicTacToeMove> minimaxMoves = new ArrayList<TicTacToeMove>(9);
        
        minimax.setListener(new Listener<TicTacToeMove>() {

            @Override
            public void onMove(Minimax<TicTacToeMove> ia, TicTacToeMove move, int turn) {
                minimaxMoves.add(move);
            }

            @Override
            public void onGameOver(Minimax<TicTacToeMove> ia) {}

            @Override
            public void onNoPossibleMove(Minimax<TicTacToeMove> ia) {}
            
        });
        
        minimax.run();
        final List<TicTacToeMove> moves = new ArrayList<TicTacToeMove>();
        
        Listener<TicTacToeMove> listener = new Listener<TicTacToeMove>() {

            @Override
            public void onMove(Minimax<TicTacToeMove> ia, TicTacToeMove move, int turn) {
                TicTacToeMove minimaxMove = moves.remove(0);
                Assert.assertEquals("Wrong player for turn " + turn, minimaxMove.getPlayer(), move.getPlayer());
                Assert.assertEquals("Wrong column for turn " + turn, minimaxMove.getX(), move.getX());
                Assert.assertEquals("Wrong row for turn " + turn, minimaxMove.getY(), move.getY());
            }

            @Override
            public void onGameOver(Minimax<TicTacToeMove> ia) {}

            @Override
            public void onNoPossibleMove(Minimax<TicTacToeMove> ia) {}
            
        };

        alphabeta.setListener(listener);
        negamax.setListener(listener);
        negascout.setListener(listener);
        minimaxTransposition.setListener(listener);

        // classic
        moves.clear();
        moves.addAll(minimaxMoves);
        alphabeta.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negamax.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negascout.run();
        
        // transposition
        moves.clear();
        moves.addAll(minimaxMoves);
        minimaxTransposition.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        alphabetaTransposition.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negamaxTransposition.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negascoutTransposition.run();
    }
    
}
