package fr.avianey.minimax4j.ext;

/**
 * http://en.wikipedia.org/wiki/Hamming_weight
 * @author antoine vianey
 */
public class Bitboard {

    // for 64 bit numbers
    public static long numberOfSetBits64(long i) {
        i = i - ((i >>> 1) & 0x5555555555555555L);
        i = (i & 0x3333333333333333L) + ((i >>> 2) & 0x3333333333333333L);
        i = ((i + (i >>> 4)) & 0x0F0F0F0F0F0F0F0FL);
        return (i * (0x0101010101010101L)) >>> 56;
    }

    // for 32 bit integers
    public static int numberOfSetBits32(int i) {
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = ((i + (i >>> 4)) & 0x0F0F0F0F);
        return (i * (0x01010101)) >>> 24;
    }

}