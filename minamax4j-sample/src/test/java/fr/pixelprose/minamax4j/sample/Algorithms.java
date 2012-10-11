package fr.pixelprose.minamax4j.sample;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import fr.pixelprose.minimax4j.IA;
import fr.pixelprose.minimax4j.IA.Algorithm;
import fr.pixelprose.minimax4j.sample.SampleRunner;
import fr.pixelprose.minimax4j.sample.SampleRunner.Listener;
import fr.pixelprose.minimax4j.sample.tictactoe.TicTacToeIA;
import fr.pixelprose.minimax4j.sample.tictactoe.TicTacToeMove;

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
public class Algorithms extends TestCase {

    public void testTicTacToe() {
        for (int depth = 1; depth <= 9; depth++) {
            
            SampleRunner<TicTacToeMove> minimax = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.MINIMAX, depth)) {};
            SampleRunner<TicTacToeMove> alphabeta = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.ALPHA_BETA, depth)) {};
            SampleRunner<TicTacToeMove> negamax = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.NEGAMAX, depth)) {};
            SampleRunner<TicTacToeMove> negascout = new SampleRunner<TicTacToeMove>(new TicTacToeIA(Algorithm.NEGASCOUT, depth)) {};
            
            final List<TicTacToeMove> minimaxMoves = new ArrayList<TicTacToeMove>(9);
            
            minimax.setListener(new Listener<TicTacToeMove>() {
    
                @Override
                public void onMove(IA<TicTacToeMove> ia, TicTacToeMove move) {
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
                public void onMove(IA<TicTacToeMove> ia, TicTacToeMove move) {
                    TicTacToeMove minimaxMove = moves.remove(0);
                    assertEquals(move.getPlayer(), minimaxMove.getPlayer());
                    assertEquals(move.getX(), minimaxMove.getX());
                    assertEquals(move.getY(), minimaxMove.getY());
                }
    
                @Override
                public void onGameOver(IA<TicTacToeMove> ia) {}
    
                @Override
                public void onNoPossibleMove(IA<TicTacToeMove> ia) {}
                
            };
    
            alphabeta.setListener(listener);
            negamax.setListener(listener);
            negascout.setListener(listener);
    
            moves.addAll(minimaxMoves);
            alphabeta.run();
            moves.addAll(minimaxMoves);
            negamax.run();
            moves.addAll(minimaxMoves);
            negascout.run();
        }
    }
    
}
