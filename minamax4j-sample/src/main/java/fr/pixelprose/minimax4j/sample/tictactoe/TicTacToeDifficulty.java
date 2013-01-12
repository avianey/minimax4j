package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.Difficulty;

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
 * TicTacToe difficulty...
 * 
 * @author antoine vianey
 */
public class TicTacToeDifficulty implements Difficulty {

    private int depth;

    public TicTacToeDifficulty(int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Depth must be > 0");
        }
        this.depth = depth;
    }
    
    @Override
    public int getDepth() {
        return depth;
    }

}
