package fr.avianey.minimax4j.ext;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.VoiceStatus;

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.Move;
import fr.avianey.minimax4j.Transposition;

/**
 * An {@link IA} backed by a <a href="http://en.wikipedia.org/wiki/Transposition_table">transposition table</a>
 * to speed up the search of the game tree.
 * 
 * @author Tonio
 *
 * @param <M> the {@link Move} implementation
 * @param <T> the transposition {@link Object}
 * @param <G> the transposition group. 
 * @see Transposition
 */
public abstract class TranspositionTableBackedIA<M extends Move, T extends Transposition, G extends Comparable<G>> extends IA<M> {
	
	private static abstract class TranspositionTableFactory<X> {
		abstract Map<X, Double> newTransposition();
	}
    
    private final TreeMap<G, Map<T, Double>> transpositionTableMap;
    private final TranspositionTableFactory<T> transpositionTableFactory;

    public TranspositionTableBackedIA() {
        super();
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>();
			}
        };
    }

	public TranspositionTableBackedIA(Algorithm algo) {
        super(algo);
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>();
			}
        };
    }

    public TranspositionTableBackedIA(Algorithm algo, final int initialCapacity) {
        super(algo);
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>(initialCapacity);
			}
        };
    }

    public TranspositionTableBackedIA(Algorithm algo, final int initialCapacity, final float loadFactor) {
        super(algo);
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>(initialCapacity, loadFactor);
			}
        };
    }

    @SuppressWarnings("unchecked")
	private TreeMap<G, Map<T, Double>> initTranspositionTableMap() {
    	Class<G> cls = (Class<G>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[2];
        if (cls.isAssignableFrom(Comparable.class)) {
        	// the transposition Group type is Comparable
        	return new TreeMap<G, Map<T,Double>>();
        } else if (cls.isAssignableFrom(Void.class)) {
        	// no transposition Group required
        	// use everything-is-equal Comparator
        	return new TreeMap<G, Map<T,Double>>(new Comparator<G>() {
				@Override
				public int compare(G o1, G o2) {
					return 0;
				}
        	});
        } else {
        	throw new IllegalArgumentException("The transposition group type : " + cls.getSimpleName() + " is neither Void nor implement the java.lang.Comparable interface.");
        }
	}

    @Override
    public M getBestMove() {
    	G currentGroup = getGroup();
    	if (currentGroup != null) {
            // evict unnecessary transpositions
    		transpositionTableMap.headMap(currentGroup).clear();
    	}
        return super.getBestMove();
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
                Map<T, Double> transpositionTable = transpositionTableMap.get(getGroup());
                if (transpositionTable != null && transpositionTable.containsKey(t)) {
                    // transposition found
                    // we can stop here as we already know the value
                    // of the evaluation function
                    score = transpositionTable.get(t);
                } else {
                    score = minimax(null, depth + 1, DEPTH);
                    if (transpositionTable == null) {
                    	transpositionTable = transpositionTableFactory.newTransposition();
                    	transpositionTableMap.put(getGroup(), transpositionTable);
                    }
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
                Map<T, Double> transpositionTable = transpositionTableMap.get(getGroup());
                if (transpositionTable != null && transpositionTable.containsKey(t)) {
                    // transposition found
                    // we can stop here as we already know the value
                    // of the evaluation function
                    score = transpositionTable.get(t);
                } else {
                    score = minimax(null, depth + 1, DEPTH);
                    if (transpositionTable == null) {
                    	transpositionTable = transpositionTableFactory.newTransposition();
                    	transpositionTableMap.put(getGroup(), transpositionTable);
                    }
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
