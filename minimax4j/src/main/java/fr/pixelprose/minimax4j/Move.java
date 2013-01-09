package fr.pixelprose.minimax4j;

/*
 *  This file is part of minimax4j.
 *  <https://github.com/avianey/minimax4j>
 *  
 *  Copyright (C) 2012 Antoine Vianey
 *  
 *  minimax4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  minimax4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with minimax4j. If not, see <http://www.gnu.org/licenses/>
 */

/**
 * 
 * Implement this interface to describe a Move in your game.<br>
 * A typical implementation for a Chess game would be :
 * <ul>
 * <li>The color of the piece</li>
 * <li>The type of the piece (king, queen, pawn, ...)</li>
 * <li>The position before the move</li>
 * <li>The position after the move</li>
 * </ul>
 * Additional information might be necessary to implement the abstract {@link IA#unmakeMove(Move)} method of the {@link IA} class :
 * <ul>
 * <li>Taken pieces</li>
 * <li>...</li>
 * </ul>
 * 
 * @author antoine vianey
 * @see IA#unmakeMove(Move)
 *
 */
public interface Move {}
