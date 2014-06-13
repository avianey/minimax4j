package fr.avianey.minimax4j.ext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.Move;

/**
 * An {@link IA} backed by a <a href="http://en.wikipedia.org/wiki/Transposition_table">transposition table</a>
 * to speed up the search of the game tree.
 * 
 * @author Tonio
 *
 * @param <M> the {@link Move} implementation
 * @param <T> the transposition {@link Object}
 * @param <G> the transposition group. 
 * @see #getGroup()
 */
// TODO : abstract Transposition type to handle symetric game of perfect informations
// in such game evA = -evB for the same position so let store 1 or -1 as a coeff in the transposition that does not take effect in the hash or equals
public abstract class TranspositionTableBackedIA<M extends Move, T, G extends Comparable<G>> extends IA<M> {
    
    private final Map<T, Double> transpositionTable;

    public TranspositionTableBackedIA() {
        super();
        this.transpositionTable = new HashMap<T, Double>();
    }

    public TranspositionTableBackedIA(Algorithm algo) {
        super(algo);
        this.transpositionTable = new HashMap<T, Double>();
    }

    public TranspositionTableBackedIA(Algorithm algo, int initialCapacity) {
        super(algo);
        this.transpositionTable = new HashMap<T, Double>(initialCapacity);
    }

    public TranspositionTableBackedIA(Algorithm algo, int initialCapacity, float loadFactor) {
        super(algo);
        this.transpositionTable = new HashMap<T, Double>(initialCapacity, loadFactor);
    }

    /**
     * Represent the current configuration by an int value.
     * <ul>
     * <li><a href="http://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing</a></li>
     * </ul>
     * @return
     *      the hash for the current configuration
     */
    public abstract T getTransposition();
    
    /**
     * Represent the group in which the current hash belong.<br/>
     * Groups can be use to lower the number of hash stored in memory :
     * <dl>
     * <dt>Reversi</dt>
     * <dd>Hash can be group by number of discs on the board</dd>
     * <dt>Chess</dt>
     * <dd>Hash can be group by number of left pieces of each color on the board</dd>
     * <dt>Connect Four</dt>
     * <dd>Hash can be group by number of dropped discs</dd>
     * <dt>...</dt>
     * </dl>
     * Groups <b>MUST</b> be ordered such as when the current configuration hash belong to group
     * G1, hashes belonging to groups G < G1 can be forgiven... 
     *
     * @return
     *      the group for the current position
     */
    public G getGroup() {
        return null;
    }
    
    
    
    
    
    

    
    protected final double minimax(final IAMoveWrapper wrapper, int depth, int DEPTH) {
        if (depth == DEPTH) {
            return evaluate();
        } else if (isOver()) {
            // if depth not reach, must consider who's playing
            return (((DEPTH - depth) % 2) == 1 ? -1 : 1) * evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return minimax(null, depth + 1, DEPTH);
        }
        if (depth % 2 == DEPTH % 2) {
            double score = -maxEvaluateValue();
            double bestScore = -maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                T t = getTransposition();
                if (transpositionTable.containsKey(t)) {
                    // transposition found
                    // we can stop here as we already know the value
                    // of the evaluation function
                    score = transpositionTable.get(t);
                } else {
                    score = minimax(null, depth + 1, DEPTH);
                    transpositionTable.put(t, score);
                }
                unmakeMove(move);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return bestScore;
        } else {
            double score = maxEvaluateValue();
            double bestScore = maxEvaluateValue();
            for (M move : moves) {
                makeMove(move);
                T t = getTransposition();
                if (transpositionTable.containsKey(t)) {
                    // transposition found
                    // we can stop here as we already know the value
                    // of the evaluation function
                    score = transpositionTable.get(t);
                } else {
                    score = minimax(null, depth + 1, DEPTH);
                    transpositionTable.put(t, score);
                }
                unmakeMove(move);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return bestScore;
        }
    }
    
    
    
    
    
}
