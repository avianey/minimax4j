/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *
 * Copyright (C) 2012 - 2015 Antoine Vianey
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
package fr.avianey.minimax4j.ia;

import fr.avianey.minimax4j.Minimax;

import java.util.List;

/**
 * Basic IA to test behaviour and speed of the various implementations.
 * We simulate a game where each cell of a 8x8 grid has a score corresponding
 * to its index in the grid. When the grid is filled by the players, the winner
 * is the player with the highest score. Each move score the value of the cell +
 * a fraction of the value depending of the turn in the game so that the IA is
 * expected to always play the cell with the highest value...
 *
 * @author antoine vianey
 */
public class IA extends Minimax<IAMove> {

    private final Logic logic;
    private final State state;

    public IA(Algorithm algo, int nbPlayers) {
        super(algo);
        logic = new Logic(nbPlayers);
        state = new State(nbPlayers);
    }

    @Override
    public boolean isOver() {
        return logic.isOver(state);
    }

    @Override
    public void makeMove(IAMove move) {
        state.makeMove(move);
    }

    @Override
    public void unmakeMove(IAMove move) {
        state.unmakeMove(move);
    }

    @Override
    public List<IAMove> getPossibleMoves() {
        return logic.getPossibleMoves(state);
    }

    @Override
    public double evaluate() {
        return logic.evaluate(state);
    }

    @Override
    public double maxEvaluateValue() {
        return logic.maxEvaluateValue();
    }

    @Override
    public void next() {
        state.next();
    }

    @Override
    public void previous() {
        state.previous();
    }
}
