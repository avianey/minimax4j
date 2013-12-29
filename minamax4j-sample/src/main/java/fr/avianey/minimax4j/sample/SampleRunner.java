package fr.avianey.minimax4j.sample;

import java.util.List;

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.Move;

/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012 Antoine Vianey
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
 * An abstract utility class for testing IA implementations.
 * 
 * @author antoine vianey
 *
 * @param <M>
 */
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
