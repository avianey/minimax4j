package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.sample.SampleRunner;

public class TicTacToeRunner extends SampleRunner {

    public TicTacToeRunner() {
        super(new TicTacToeIA(2));
    }
    
    public static void main(String[] args) {
        SampleRunner runner = new TicTacToeRunner();
        runner.run();
    }
    
}
