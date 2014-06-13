package fr.avianey.minimax4j;

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
 * <p>
 * Implement this interface if you want to use the {@link TranspositionTableBackedIA}.<br/>
 * Implementation MUST re-define {@link Object#equals(Object)} &amp; {@link Object#hashcode()}
 * </p>
 * <p>
 * The current player MUST be taken in account in the transposition's {@link Object#equals(Object)} function
 * otherwise the stored value for the transposition may reflect the strength of the other player...
 * </p>
 * 
 * <h4>Symmetric games</h4>
 * In symmetric games, it is possible to take advantage of the fact that an evaluation for a given
 * transposition can give the evaluation for the symmetric transposition where the position of the 
 * players are interchanged.
 * 
 * <h4>Symmetric boards</h4>
 * In games where boards are symmetric (ie : where a symetry can be apply to the board without
 * modifying the position's strength of the players), it is possible to take advantage of the fact 
 * that an evaluation for a given transposition can give the evaluation for the symmetric transposition 
 * where a symmetry is applied to the board.
 * 
 * @author avianey
 * @see TranspositionTableBackedIA
 */
public interface Transposition {}