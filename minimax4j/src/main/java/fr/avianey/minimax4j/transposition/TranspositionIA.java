package fr.avianey.minimax4j.transposition;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.avianey.minimax4j.IA;
import fr.avianey.minimax4j.Move;


/*
 * This file is part of minimax4j.
 * <https://github.com/avianey/minimax4j>
 *  
 * Copyright (C) 2012, 2013, 2014 Antoine Vianey
 * 
 * minimax4j is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * minimax4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with minimax4j. If not, see <http://www.gnu.org/licenses/lgpl.html>
 */


/**
 * An {@link IA} backed by a <a href="http://en.wikipedia.org/wiki/Transposition_table">transposition table</a>
 * to speed up the search of the game tree.
 * 
 * @author antoine vianey
 *
 * @param <M> the {@link Move} implementation
 * @param <T> the {@link Transposition} implementation
 * @param <G> the transposition group implementation. 
 * @see Transposition
 */
// TODO Enhanced transposition cutoffs
// TODO Custom TranspositionTableFactory
public abstract class TranspositionIA<M extends Move, T extends Transposition, G extends Comparable<G>> extends IA<M> {
	
	private static abstract class TranspositionTableFactory<X> {
		abstract Map<X, Double> newTransposition();
	}
    
    private final TreeMap<G, Map<T, Double>> transpositionTableMap;
    private final TranspositionTableFactory<T> transpositionTableFactory;

    public TranspositionIA() {
        super();
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>();
			}
        };
    }

	public TranspositionIA(Algorithm algo) {
        super(algo);
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>();
			}
        };
    }

    public TranspositionIA(Algorithm algo, final int initialCapacity) {
        super(algo);
        transpositionTableMap = initTranspositionTableMap();
        transpositionTableFactory = new TranspositionTableFactory<T>() {
			@Override
			Map<T, Double> newTransposition() {
				return new HashMap<T, Double>(initialCapacity);
			}
        };
    }

    public TranspositionIA(Algorithm algo, final int initialCapacity, final float loadFactor) {
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
        Type t = getClass().getGenericSuperclass();
        // search for the Group class within class hierarchy
        while (!(t instanceof ParameterizedType 
                && TranspositionIA.class.getSimpleName().equals(
                        ((Class<?>) ((ParameterizedType) t).getRawType()).getSimpleName()))) {
            t = ((Class<?>) t).getGenericSuperclass();
        } 
    	Class<G> cls = (Class<G>) ((ParameterizedType) t).getActualTypeArguments()[2];
        if (Comparable.class.isAssignableFrom(cls)) {
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
    	if (clearGroupsBeforeSearch()) {
    		clearGroups(getGroup());
    	}
        M m = super.getBestMove();
    	if (clearGroupsAfterSearch()) {
    		clearGroups(getGroup());
    	}
    	return m;
    }
    
    // TODO : getBestMove() does not play the move... so the best move group won't be cleared
    private final void clearGroups(G currentGroup) {
    	if (currentGroup != null) {
    		// free memory :
            // evict unnecessary transpositions
    		transpositionTableMap.headMap(currentGroup).clear();
    	}
    }
    
    /**
     * Whether or not the remove useless transposition before the tree exploration.
     * Default to false.
     * @return
     */
    public boolean clearGroupsBeforeSearch() {
    	return false;
    }
    
    /**
     * Whether or not the remove useless transposition after the tree exploration.
     * Default to false.
     * @return
     */
    public boolean clearGroupsAfterSearch() {
    	return false;
    }
    
    /**
     * Programmaticalty reset the content of the transposition table.
     * The prefered way to free memory is to use the grouping functionnality.
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
     *      the hash for the current configuration
     *      
     * @see Transposition
     */
    public abstract T getTransposition();
    
    /**
     * Returns all the {@link Transposition} representing the current game configuration.
     * @return
     *      a {@link Collection} of {@link Transposition}
     */
    public Collection<T> getSymetricTranspositions() {
        return Collections.singleton(getTransposition());
    }
    
    /**
     * Represent the group in which the current transposition belong.<br/>
     * Groups can be use to lower the number of transposition stored in memory :
     * <dl>
     * <dt>Reversi</dt>
     * <dd>Transpositions can be group by number of discs on the board</dd>
     * <dt>Chess</dt>
     * <dd>Transpositions can be group by number of left pieces of each color on the board</dd>
     * <dt>Connect Four</dt>
     * <dd>Transpositions can be group by number of dropped discs</dd>
     * <dt>...</dt>
     * </dl>
     * Groups <b>MUST</b> be ordered such as when the current configuration hash belong to group
     * G1, transpositions that belongs to groups G < G1 can be forgiven... If you don't want to 
     * handle groups, let G be {@link Void} and return null groups.
     *
     * @return
     *      the group for the current position
     */
    public abstract G getGroup();
    
    /*=================*
     * IMPLEMENTATIONS *
     *=================*/
    
    @Override
    protected double minimax(final IAMoveWrapper wrapper, int depth, int who) {
        if (depth == 0 || isOver()) {
            return who * evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return minimax(null, depth - 1, -who);
        }
        if (who > 0) {
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
                    score = minimax(null, depth - 1, -who);
                    if (transpositionTable == null) {
                    	transpositionTable = transpositionTableFactory.newTransposition();
                    	transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
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
                    score = -transpositionTable.get(t);
                } else {
                    score = minimax(null, depth - 1, -who);
                    if (transpositionTable == null) {
                    	transpositionTable = transpositionTableFactory.newTransposition();
                    	transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
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

    @Override
    protected double alphabeta(final IAMoveWrapper wrapper, int depth, int who, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return who * evaluate();
        }
        M bestMove = null;
        double score;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return alphabeta(null, depth - 1, -who, alpha, beta);
        }
        if (who > 0) {
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
                    score = alphabeta(null, depth - 1, -who, alpha, beta);
                    if (transpositionTable == null) {
                        transpositionTable = transpositionTableFactory.newTransposition();
                        transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
                }
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        } else {
            for (M move : moves) {
                makeMove(move);
                T t = getTransposition();
                Map<T, Double> transpositionTable = transpositionTableMap.get(getGroup());
                if (transpositionTable != null && transpositionTable.containsKey(t)) {
                    // transposition found
                    // we can stop here as we already know the value
                    // of the evaluation function
                    score = -transpositionTable.get(t);
                } else {
                    score = alphabeta(null, depth - 1, -who, alpha, beta);
                    if (transpositionTable == null) {
                        transpositionTable = transpositionTableFactory.newTransposition();
                        transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
                }
                unmakeMove(move);
                if (score < beta) {
                    beta = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return beta;
        }
    }

    @Override
    protected double negamax(final IAMoveWrapper wrapper, int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        M bestMove = null;
        Collection<M> moves = getPossibleMoves();
        if (moves.isEmpty()) {
            return -negamax(null, depth - 1, -beta, -alpha);
        } else {
            double score = -maxEvaluateValue();
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
                    score = -negamax(null, depth - 1, -beta, -alpha);
                    if (transpositionTable == null) {
                        transpositionTable = transpositionTableFactory.newTransposition();
                        transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
                }
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        }
    }
    
    @Override
    protected double negascout(IAMoveWrapper wrapper, int depth, double alpha, double beta) {
        if (depth == 0 || isOver()) {
            return evaluate();
        }
        List<M> moves = getPossibleMoves();
        double b = beta;
        M bestMove = null;
        if (moves.isEmpty()) {
            return -negascout(null, depth - 1, -beta, -alpha);
        } else {
            double score;
            boolean first = true;
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
                    score = -negascout(null, depth - 1, -b, -alpha);
                    if (!first && alpha < score && score < beta) {
                        score = -negascout(null, depth - 1, -beta, -alpha);
                    }
                    if (transpositionTable == null) {
                        transpositionTable = transpositionTableFactory.newTransposition();
                        transpositionTableMap.put(getGroup(), transpositionTable);
                    }
                    // save transposition
                    for (T st : getSymetricTranspositions()) {
                        transpositionTable.put(st, score);
                    }
                }
                unmakeMove(move);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                    if (alpha >= beta) {
                        break;
                    }
                }
                b = alpha + 1;
                first = false;
            }
            if (wrapper != null) {
                wrapper.move = bestMove;
            }
            return alpha;
        }
    }
    
}
