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
import org.omg.PortableInterceptor.LOCATION_FORWARD;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Negamax based with transposition table implementation.
 *
 * @author antoine vianey
 *
 * @param <M> Implementation of the Move interface to use
 */
public abstract class TranspositionNegamax<M extends Move, T, G> extends Negamax<M> {

    private static final int FLAG_EXACT = 0;
    private static final int FLAG_UPPERBOUND = 1;
    private static final int FLAG_LOWERBOUND = 2;

    protected static class Transposition {
        private final double value;
        private final int depth;
        private final int flag;

        private Transposition(double value, int depth, int flag) {
            this.value = value;
            this.depth = depth;
            this.flag = flag;
        }
    }

    /**
     * Factory for transposition table.
     * Unless {@link #TranspositionNegamax(TranspositionTableFactory)} is used as super constructor,
     * an {@link HashMap} is used as default implementation.
     *
     * @author antoine vianey
     *
     * @param <T>
     */
    public interface TranspositionTableFactory<T> {
        Map<T, Transposition> newTranspositionTable();
    }

    private final transient TreeMap<G, Map<T, Transposition>> transpositionTableMap;
    private final transient TranspositionTableFactory<T> transpositionTableFactory;

    public TranspositionNegamax() {
        this(new TranspositionTableFactory<T>() {
            @Override
            public Map<T, Transposition> newTranspositionTable() {
                return new HashMap<>();
            }
        });
    }

    public TranspositionNegamax(final int initialCapacity) {
        super();
        this.transpositionTableMap = initTranspositionTableMap();
        this.transpositionTableFactory = new TranspositionTableFactory<T>() {
            @Override
            public Map<T, Transposition> newTranspositionTable() {
                return new HashMap<>(initialCapacity);
            }
        };
    }

    public TranspositionNegamax(final int initialCapacity, final float loadFactor) {
        super();
        this.transpositionTableMap = initTranspositionTableMap();
        this.transpositionTableFactory = new TranspositionTableFactory<T>() {
            @Override
            public Map<T, Transposition> newTranspositionTable() {
                return new HashMap<>(initialCapacity, loadFactor);
            }
        };
    }

    public TranspositionNegamax(TranspositionTableFactory<T> transpositionTableFactory) {
        super();
        this.transpositionTableMap = initTranspositionTableMap();
        this.transpositionTableFactory = transpositionTableFactory;
    }

    /**
     * Initialize the map of transposition table classified by groups.
     * @return
     * 		A {@link TreeMap} storing transposition tables by group
     */
    @SuppressWarnings("unchecked")
    private TreeMap<G, Map<T, Transposition>> initTranspositionTableMap() {
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

    public TreeMap<G, Map<T, Transposition>> getTranspositionTableMap() {
        return this.transpositionTableMap;
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
    public final void clearTranspositionTable() {
        transpositionTableMap.clear();
    }

    /**
     * Represent the current configuration by an int value.
     * <ul>
     * <li><a href="http://en.wikipedia.org/wiki/Zobrist_hashing">Zobrist hashing</a></li>
     * </ul>
     * The current player MUST be taken in account in the transposition's {@link Object#equals(Object)} function
     * otherwise the stored value for the transposition may reflect the strength of the other player...
     * @return
     *      the value for the current configuration
     */
    public abstract T getTranspositionValue();

    /**
     * Returns all the transpositions representing the current game configuration.
     * @return
     *      a {@link Iterable} of transpositions
     */
    public Iterable<T> getSymmetricTranspositionValues() {
        return Collections.singleton(getTranspositionValue());
    }

    /**
     * Represent the group in which the current transposition belong.<br/>
     * Groups can be use to lower the number of transposition stored in memory :
     * <dl>
     * <dt>Reversi</dt>
     * <dd>Transpositions can be grouped by number of discs on board</dd>
     * <dt>Chess</dt>
     * <dd>Transpositions can be grouped by number of left pieces of each color on the board</dd>
     * <dt>Connect Four</dt>
     * <dd>Transpositions can be grouped by number of dropped discs</dd>
     * <dt>...</dt>
     * </dl>
     * Groups <b>MUST</b> be ordered such as when the current configuration hash belong to group
     * G1, transpositions that belongs to groups G < G1 can be forgiven... If you don't want to
     * handle groups, let G be {@link Void} and return null.
     *
     * @return
     *      the group for the current position
     */
    public abstract G getGroup();

    private void saveTransposition(Map<T, Transposition> transpositionTable, final int depth, final double score, final int flag) {
        if (transpositionTable == null) {
            transpositionTable = transpositionTableFactory.newTranspositionTable();
            transpositionTableMap.put(getGroup(), transpositionTable);
        }
        // save transposition
        Transposition transposition = new Transposition(score, depth, flag);
        for (T st : getSymmetricTranspositionValues()) {
            transpositionTable.put(st, transposition);
        }
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
        Map<T, Transposition> transpositionTable = transpositionTableMap.get(getGroup());
        if (transpositionTable != null) {
            Transposition transposition = transpositionTable.get(getTranspositionValue());
            if (transposition != null && depth <= transposition.depth) {
                double value = (((transposition.depth - depth) & 1) == 0 ? 1 : -1) * transposition.value;
                switch (transposition.flag) {
                    case FLAG_EXACT:
                        // transposition has a deeper or equal search depth
                        // we can stop here as we already know the value
                        // returned by the evaluation function
                        return value;
                    case FLAG_UPPERBOUND:
                        if (value < beta) {
                            b = value;
                        }
                        break;
                    case FLAG_LOWERBOUND:
                        if (value > alpha) {
                            a = value;
                        }
                        break;
                }
                if (a >= b) {
                    return value;
                }
            }
        }

        double score = super.negamax(wrapper, depth, a, b);

        if (score <= a) {
            saveTransposition(transpositionTable, depth, score, FLAG_UPPERBOUND);
        } else if (score >= beta) {
            saveTransposition(transpositionTable, depth, score, FLAG_LOWERBOUND);
        } else {
            saveTransposition(transpositionTable, depth, score, FLAG_EXACT);
        }

        return score;
	}

}
