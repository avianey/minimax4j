package fr.avianey.minimax4j.ext;

/**
 * http://en.wikipedia.org/wiki/Hamming_weight
 * @author antoine vianey
 */
public class Bitboard {

    /**
     * Count number of bits set to one in the given long (64-bit integers).
     * @param i
     * @return
     */
    public static long numberOfSetBits64_1(long i) {
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = ((i + (i >>> 4)) & 0x0F0F0F0F0F0F0F0FL);
        return (i * (0x0101010101010101L)) >>> 56;
    }
    
    /**
     * Count number of bits set to one in the given long (64-bit integers).<br/>
     * Faster when only few bits are set to one.
     * @param i
     * @return
     */
    public static long numberOfSetBits64_2(long i) {
        int count = 0;
        while (i != 0) {
        	i &= (i - 1L);
        	count++;
        }
        return count;
    }

    /**
     * Count number of bits set to one in the given int (32-bit integers).
     * @param i
     * @return
     */
    public static int numberOfSetBits32(int i) {
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = ((i + (i >>> 4)) & 0x0F0F0F0F);
        return (i * (0x01010101)) >>> 24;
    }

    /**
     * Count number of bits set to one in the given int (32-bit integers).<br/>
     * Faster when only few bits are set to one.
     * @param i
     * @return
     */
    public static long numberOfSetBits32_2(int i) {
        int count = 0;
        while (i != 0) {
        	i &= i-1;
        	count++;
        }
        return count;
    }

}