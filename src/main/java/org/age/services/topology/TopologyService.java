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
/*
 * Created: 2014-10-30
 */

package org.age.services.topology;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Optional;
import java.util.Set;

public interface TopologyService {

	/**
	 * Returns a set of neighbours of the current node.
	 *
	 * @return set of {@link String}, possibly empty when there is no topology or the node has no neighbours.
	 *
	 * @throws IllegalStateException
	 * 		when the topology cannot get the list of neighbours because it has not finished discovery.
	 */
	@NonNull Set<String> neighbours();

	/**
	 * Returns the current topology graph.
	 *
	 * @return an Optional containing the topology graph or empty when no topology was set.
	 */
	@NonNull Optional<DirectedGraph<String, DefaultEdge>> topologyGraph();

	@NonNull Optional<String> topologyType();

	@NonNull Optional<String> masterId();

	boolean hasTopology();
}
