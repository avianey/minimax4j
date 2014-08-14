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

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.IA.Algorithm;
import fr.avianey.minimax4j.sample.SampleRunner;
import fr.avianey.minimax4j.sample.SampleRunner.Listener;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeIA;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeMove;
import fr.avianey.minimax4j.sample.tictactoe.TicTacToeTranspositionIA;

/**
 * Check for every sample game that the same game is played exactly the same for each Algorithm :<br/>
 * <ul>
 * <li>Minimax</li>
 * <li>Alpha-beta</li>
 * <li>Negamax</li>
 * <li>Negascout</li>
 * </ul>
 * 
 * @author avianey
 * @see IA
 * @see IA.Algorithm
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

	@Test
    public void testTicTacToe() {
		// normal IA
        SampleRunner<TicTacToeMove> minimax = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.MINIMAX, depth)) {};
        SampleRunner<TicTacToeMove> alphabeta = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.ALPHA_BETA, depth)) {};
        SampleRunner<TicTacToeMove> negamax = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.NEGAMAX, depth)) {};
        SampleRunner<TicTacToeMove> negascout = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.NEGASCOUT, depth)) {};
        // transposition Table backed IA
        SampleRunner<TicTacToeMove> minimaxTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionIA(Algorithm.MINIMAX, depth)) {};
        SampleRunner<TicTacToeMove> alphabetaTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionIA(Algorithm.ALPHA_BETA, depth)) {};
        SampleRunner<TicTacToeMove> negamaxTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionIA(Algorithm.NEGAMAX, depth)) {};
        SampleRunner<TicTacToeMove> negascoutTransposition = new SampleRunner<TicTacToeMove>(new TicTacToeTranspositionIA(Algorithm.NEGASCOUT, depth)) {};
        
        final List<TicTacToeMove> minimaxMoves = new ArrayList<TicTacToeMove>(9);
        
        minimax.setListener(new Listener<TicTacToeMove>() {

            @Override
            public void onMove(IA<TicTacToeMove> ia, TicTacToeMove move, int turn) {
                minimaxMoves.add(move);
            }

            @Override
            public void onGameOver(IA<TicTacToeMove> ia) {}

            @Override
            public void onNoPossibleMove(IA<TicTacToeMove> ia) {}
            
        });
        
        minimax.run();
        final List<TicTacToeMove> moves = new ArrayList<TicTacToeMove>();
        
        Listener<TicTacToeMove> listener = new Listener<TicTacToeMove>() {

            @Override
            public void onMove(IA<TicTacToeMove> ia, TicTacToeMove move, int turn) {
                TicTacToeMove minimaxMove = moves.remove(0);
                Assert.assertEquals("Wrong player for turn " + turn, minimaxMove.getPlayer(), move.getPlayer());
                Assert.assertEquals("Wrong column for turn " + turn, minimaxMove.getX(), move.getX());
                Assert.assertEquals("Wrong row for turn " + turn, minimaxMove.getY(), move.getY());
            }

            @Override
            public void onGameOver(IA<TicTacToeMove> ia) {}

            @Override
            public void onNoPossibleMove(IA<TicTacToeMove> ia) {}
            
        };

        alphabeta.setListener(listener);
        negamax.setListener(listener);
        negascout.setListener(listener);
        minimaxTransposition.setListener(listener);

        moves.clear();
        moves.addAll(minimaxMoves);
        alphabeta.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negamax.run();
        moves.clear();
        moves.addAll(minimaxMoves);
        negascout.run();
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
