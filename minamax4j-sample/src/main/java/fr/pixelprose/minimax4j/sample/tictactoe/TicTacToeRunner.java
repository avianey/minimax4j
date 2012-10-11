package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.IA.Algorithm;
import fr.pixelprose.minimax4j.sample.SampleRunner;

/*
 *  This file is part of minimax4j.
 *  <https://github.com/avianey/minimax4j>
 *  
 *  Copyright (C) 2012 Antoine Vianey
 *  
 *  minimax4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  minimax4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with minimax4j. If not, see <http://www.gnu.org/licenses/>
 */

/**
 * Run a game between two TicTacToeIA opponent...
 * 
 * @author antoine vianey
 */
public class TicTacToeRunner extends SampleRunner<TicTacToeMove> {

    public TicTacToeRunner() {
        // Change the thinking depth value > 0
        super(new TicTacToeIA(Algorithm.MINIMAX, 2));
    }
    
    public static void main(String[] args) {
        SampleRunner<TicTacToeMove> runner = new TicTacToeRunner();
        runner.run();
    }
    
}
