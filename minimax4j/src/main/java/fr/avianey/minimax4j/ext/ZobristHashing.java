package fr.avianey.minimax4j.ext;

import fr.avianey.minimax4j.transposition.Transposition;
import fr.avianey.minimax4j.transposition.TranspositionIA;

/**
 * A simple implementation of the <a href="http://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist Hash</a>
 * Usefull for {@link TranspositionIA#getTransposition()} and {@link Transposition#hashCode()}
 * 
 * @author Tonio
 * @see Transposition
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
                bitStrings[i][j] = (int) (Math.random() * Long.MAX_VALUE);
            }
        }
        hash = 0;
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
