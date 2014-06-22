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
 * Implement this interface to provide several difficulties for your IA implementation.
 * 
 * @author avianey
 */
public interface Difficulty {

    /**
     * The thinking depth for the decision rule used by the IA.<br/>
     * Implementation of Difficulty must return a value greater or equal to one (&gt;0)
     * @return
     *         The thinking depth of the IA.
     */
    public int getDepth();
    
}
