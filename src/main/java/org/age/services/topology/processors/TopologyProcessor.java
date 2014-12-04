/*
 * Copyright (C) 2014 Intelligent Information Systems Group.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.age.services.topology.processors;

import org.age.services.identity.NodeDescriptor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;

/**
 * Topology processor generates a topology graph from the given set of nodes.
 */
@FunctionalInterface
public interface TopologyProcessor {

	/**
	 * Returns a priority of a processor (higher is more important).
	 *
	 * Used for selecting the initial processor.
	 *
	 * By default returns 0.
	 */
	default int priority() {
		return 0;
	}

	/**
	 * Return a name of the processor.
	 *
	 * By default returns an empty string.
	 */
	default @NonNull String name() {
		return "";
	}

	/**
	 * Returns a graph of node connections based on the given set of nodes.
	 *
	 * @param identities
	 * 		node identities.
	 *
	 * @return a directed graph of node connections.
	 */
	@NonNull DirectedGraph<String, DefaultEdge> createGraphFrom(@NonNull Set<? extends NodeDescriptor> identities);
}
