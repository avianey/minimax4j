package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.IA.Algorithm;
import fr.pixelprose.minimax4j.sample.SampleRunner;

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
