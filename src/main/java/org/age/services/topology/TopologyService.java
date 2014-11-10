/*
 * Created: 2014-10-30
 * $Id$
 */

package org.age.services.topology;

import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

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
