/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * The MIT License (MIT)

 * Copyright (c) 2015 Antoine Vianey
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fr.avianey.minimax4j.impl;

import fr.avianey.minimax4j.Move;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Negamax based with transposition table implementation.
 *
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 * @param <K> The key used for the transposition table
 * @param <G> An optional {@link Comparable<G>} use to group transpositions
 */
public abstract class TranspositionNegamax<M extends Move, K, G> extends Negamax<M> {

    protected static final int FLAG_EXACT = 0;
    protected static final int FLAG_UPPERBOUND = 1;
    protected static final int FLAG_LOWERBOUND = 2;

    protected static class Transposition<K> {
        private final double value;
        private final int depth;
        private final int flag;
        private final K key;

        protected Transposition(double value, int depth, int flag, K key) {
            this.value = value;
            this.depth = depth;
            this.flag = flag;
            this.key = key;
        }
    }

    public interface TranspositionTableFactory<K> {
        Map<Integer, Transposition<K>> newTranspositionTable();
    }

    private final transient TreeMap<G, Map<Integer, Transposition<K>>> transpositionTableMap;
    private final transient TranspositionTableFactory<K> transpositionTableFactory;

    public TranspositionNegamax() {
        this(new TranspositionTableFactory<K>() {
            @Override
            public Map<Integer, Transposition<K>> newTranspositionTable() {
                return new HashMap<>();
            }
        });
    }

    public TranspositionNegamax(final int initialCapacity) {
        this(new TranspositionTableFactory<K>() {
            @Override
            public Map<Integer, Transposition<K>> newTranspositionTable() {
                return new HashMap<>(initialCapacity);
            }
        });
    }

    public TranspositionNegamax(final int initialCapacity, final float loadFactor) {
        this(new TranspositionTableFactory<K>() {
            @Override
            public Map<Integer, Transposition<K>> newTranspositionTable() {
                return new HashMap<>(initialCapacity, loadFactor);
            }
        });
    }

    public TranspositionNegamax(TranspositionTableFactory<K> transpositionTableFactory) {
        super();
        this.transpositionTableFactory = transpositionTableFactory;
        this.transpositionTableMap = initTranspositionTableMap();
    }


    /**
     * Initialize the map of transposition table classified by groups.
     * @return
     * 		A {@link TreeMap} storing transposition tables by group
     */
    @SuppressWarnings("unchecked")
    private TreeMap<G, Map<Integer, Transposition<K>>> initTranspositionTableMap() {
        Type t = getClass().getGenericSuperclass();
        // search for the Group class within class hierarchy
        while (!(t instanceof ParameterizedType
                && TranspositionNegamax.class.getSimpleName().equals(
                ((Class<?>) ((ParameterizedType) t).getRawType()).getSimpleName()))) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        Class<G> cls = (Class<G>) ((ParameterizedType) t).getActualTypeArguments()[2];
        if (Comparable.class.isAssignableFrom(cls)) {
            // the transposition Group type is Comparable
            return new TreeMap<>();
        } else if (cls.isAssignableFrom(Void.class)) {
            // no transposition Group required
            // use everything-is-equal Comparator
            return new TreeMap<>(new Comparator<G>() {
                @Override
                public int compare(G o1, G o2) {
                    return 0;
                }
            });
        } else {
            throw new IllegalArgumentException("The transposition group type : " + cls.getSimpleName() + " is neither Void nor implement the java.lang.Comparable interface.");
        }
    }

    private void clearGroups(G currentGroup) {
        if (currentGroup != null) {
            // free memory :
            // evict unnecessary transpositions
            transpositionTableMap.headMap(currentGroup).clear();
        }
    }

    /**
     * Reset the content of the transposition table.
     * The preferred way to free memory is to use the grouping functionality.
     *
     * @see #getGroup()
     */
    public final void clearTranspositions() {
        transpositionTableMap.clear();
    }

    /**
     * Returns the current configuration key.<br/>
     * Implementation MUST override the {@link #hashCode()} and {@link #equals(Object)} methods.
     * A <a href="http://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing</a> could be use to provide
     * the hash value of the transposition key. Equality between key will be used to check for collision when
     * different configurations map to the same hash value.
     *  <dl>
     *      <dt>{@link #hashCode()}</dt>
     *      <dd>
     *          Should be tuned to adjust the number of entry in the transposition table as well as its memory footprint.
     *          Increasing the number of possible hashCode values will allow more transpositions and consume more memory.
     *      </dd>
     *      <dt>{@link #equals(Object)}</dt>
     *      <dd>
     *          Might be tuned to avoid using an invalid transposition du to collisions in the transposition table.
     *      </dd>
     *  </dl>
     *  Use {@link #saveTransposition(Object, Transposition, double, int, int)} to implement replacement strategy.
     *
     * @see #saveTransposition(Object, Transposition, double, int, int)
     * @return
     *      The key representing the current configuration.
     *      MUST be non null and preferably immutable.
     */
    public abstract K getTranspositionKey();

    /**
     * Represent the group in which the current transposition belong.<br/>
     * Groups can be use to lower the number of transposition stored in memory :
     * <dl>
     *     <dt>Reversi</dt>
     *     <dd>Transpositions can be grouped by number of discs on board</dd>
     *     <dt>Chess</dt>
     *     <dd>Transpositions can be grouped by number of left pieces of each color on the board</dd>
     *     <dt>Connect Four</dt>
     *     <dd>Transpositions can be grouped by number of dropped discs</dd>
     * </dl>
     * Groups <b>MUST</b> be ordered such as when the current configuration hash belong to group
     * G1, transpositions that belongs to groups G < G1 can be removed... If you don't want to
     * handle groups, let G be {@link Void} and return null.
     *
     * @return
     *      The group for the current position
     */
    public abstract G getGroup();

    /**
     * Save the transposition for the current position using a default strategy:
     * <ul>
     *     <li>existing transposition is null</li>
     *     <li>existing transposition depth is lower than the remaining depth</li>
     * </ul>
     * Override this method to implement your own replacement strategy using additional criteria such as ancient,
     *
     * @param key the transposition key for the current configuration
     * @param transposition the existing transposition for the given key hash (or null if no transposition exists)
     * @param score the evaluation for the given depth
     * @param depth the depth of the searched subtree (aka remaining depth)
     * @param flag type of evaluation {@link #FLAG_EXACT}, {@link #FLAG_LOWERBOUND}, {@link #FLAG_UPPERBOUND}
     */
    protected void saveTransposition(final K key, final Transposition<K> transposition, final double score, final int depth, final int flag) {
        if (transposition == null || transposition.depth <= depth) {
            getTranspositionTable().put(key.hashCode(), new Transposition<K>(score, depth, flag, key));
        }
    }

    private Map<Integer, Transposition<K>> getTranspositionTable() {
        Map<Integer, Transposition<K>> transpositionTable = transpositionTableMap.get(getGroup());
        if (transpositionTable == null) {
            transpositionTable = transpositionTableFactory.newTranspositionTable();
            transpositionTableMap.put(getGroup(), transpositionTable);
        }
        return transpositionTable;
    }

    @Override
    public M getBestMove(final int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Search depth MUST be > 0");
        }
        MoveWrapper<M> wrapper = new MoveWrapper<>();
        super.negamax(wrapper, depth, -maxEvaluateValue(), maxEvaluateValue());
        // clear useless groups
        clearGroups(getGroup());
        return wrapper.move;
    }

    @Override
    protected double negamax(final MoveWrapper<M> wrapper, final int depth, final double alpha, final double beta) {
        double a = alpha;
        double b = beta;
        K key = getTranspositionKey();
        Transposition<K> transposition = getTranspositionTable().get(key.hashCode());
        if (transposition != null && depth <= transposition.depth && key.equals(transposition.key)) {
            switch (transposition.flag) {
                case FLAG_EXACT:
                    // transposition has a deeper or equal search depth
                    // we can stop here as we already know the value
                    // returned by the evaluation function
                    return transposition.value;
                case FLAG_UPPERBOUND:
                    if (transposition.value < beta) {
                        b = transposition.value;
                    }
                    break;
                case FLAG_LOWERBOUND:
                    if (transposition.value > alpha) {
                        a = transposition.value;
                    }
                    break;
            }
            if (a >= b) {
                return transposition.value;
            }
        }

        double score = super.negamax(wrapper, depth, a, b);

        if (score <= a) {
            saveTransposition(key, transposition, score, depth, FLAG_UPPERBOUND);
        } else if (score >= beta) {
            saveTransposition(key, transposition, score, depth, FLAG_LOWERBOUND);
        } else {
            saveTransposition(key, transposition, score, depth, FLAG_EXACT);
        }

        return score;
	}

}
