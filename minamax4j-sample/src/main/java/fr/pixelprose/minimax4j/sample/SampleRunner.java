package fr.pixelprose.minimax4j.sample;

import java.util.List;

import fr.pixelprose.minimax4j.IA;
import fr.pixelprose.minimax4j.Move;

public abstract class SampleRunner {

    private IA ia;

    public SampleRunner(IA ia) {
        this.ia = ia;
    }
    
    public void run() {
        while (!ia.isOver()) {
            List<Move> moves = ia.getPossibleMoves();
            if (!moves.isEmpty()) {
                ia.makeMove(ia.getBestMove());
                System.out.println(ia.toString());
            } else {
                // no move for the current player
                // up to next player
                ia.next();
            }
        }
    }
    
}
