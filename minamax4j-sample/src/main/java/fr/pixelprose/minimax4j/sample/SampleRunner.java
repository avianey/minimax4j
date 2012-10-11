package fr.pixelprose.minimax4j.sample;

import java.util.List;

import fr.pixelprose.minimax4j.IA;
import fr.pixelprose.minimax4j.Move;

public abstract class SampleRunner<M extends Move> {
    
    public static interface Listener<M extends Move> {
        public void onMove(IA<M> ia, M move);
        public void onGameOver(IA<M> ia);
        public void onNoPossibleMove(IA<M> ia);
    }

    private IA<M> ia;
    private Listener<M> listener;

    public SampleRunner(IA<M> ia) {
        this.ia = ia;
    }
    
    public void setListener(Listener<M> listener) {
        this.listener = listener;
    }
    
    public void run() {
        M move;
        while (!ia.isOver()) {
            List<M> moves = ia.getPossibleMoves();
            if (!moves.isEmpty()) {
                move = ia.getBestMove();
                ia.makeMove(move);
                if (listener != null) {
                    listener.onMove(ia, move);
                }
            } else {
                if (listener != null) {
                    listener.onNoPossibleMove(ia);
                }
                // no move for the current player
                // up to next player
                ia.next();
            }
        }
        if (listener != null) {
            listener.onGameOver(ia);
        }
    }
    
}
