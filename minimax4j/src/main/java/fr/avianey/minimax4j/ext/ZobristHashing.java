package fr.avianey.minimax4j.ext;

import java.util.HashMap;

import fr.avianey.minimax4j.TranspositionIA;

/**
 * A simple implementation of the <a href="http://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist Hash</a>.
 * As {@link Object#hashCode()} is limited to 32-bits int precision, be aware of the highest risk of collision
 * compared to a 64-bits long based Zobrist hash implementation. This ensure compatibility with {@link HashMap}. 
 * 
 * @author Tonio
 * @see TranspositionIA
 */
public class ZobristHashing {

    private int hash;
    private final int[][] bitStrings;
    
    /**
     * Initialize values for the given number of pieces and the given number of positions
     * @param pieces
     * @param positions
     */
    public ZobristHashing(int pieces, int positions) {
        bitStrings = new int[pieces][positions];
        for (int i = 0; i < pieces; i++) {
            for (int j = 0; j < positions; j++) {
                bitStrings[i][j] = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
            }
        }
        hash = 0;
    }
    
    /**
     * Initialize values from an existing instance of {@link ZobristHashing}
     * @param from
     */
    public ZobristHashing(ZobristHashing from) {
    	this.bitStrings = from.bitStrings;
    	this.hash = from.hash;
    }

    public void reset() {
        hash = 0;
    }
    
    /**
     * Compute the resulting hash after the given move
     * @param piece
     * @param position
     * @return
     *      the Zobrist hash value
     */
    public int add(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
        return hash;
    }
    
    /**
     * Compute the resulting hash after the given move
     * @param piece
     * @param position
     * @return
     *      the Zobrist hash value
     */
    public int remove(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
        return hash;
    }
    
    @Override
    public int hashCode() {
        return hash;
    }
    
}
