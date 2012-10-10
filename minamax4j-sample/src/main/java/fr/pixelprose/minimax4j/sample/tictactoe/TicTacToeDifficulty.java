package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.Difficulty;

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
