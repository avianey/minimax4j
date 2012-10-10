package fr.pixelprose.minimax4j.sample.tictactoe;

import fr.pixelprose.minimax4j.Move;

public class TicTacToeMove implements Move {

    /** The player owning the move */
    private int player;
    
    /** x coordinate of the move */
    private int x;
    /** y coordinate of the move */
    private int y;
    
    public TicTacToeMove(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }
    
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }
    
}
