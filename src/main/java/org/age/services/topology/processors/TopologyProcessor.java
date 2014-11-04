package org.age.services.topology.processors;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.age.services.identity.NodeIdentity;

/**
 * Topology processor generates a topology graph from the given set of nodes.
 */
@FunctionalInterface
public interface TopologyProcessor {

	/**
	 * Returns a priority of a processor (higher is more important).
	 * <p>
	 * Used for selecting the initial processor.
	 * <p>
	 * By default returns 0.
	 */
	default int getPriority() {
		return 0;
	}

	/**
	 * Return a name of the processor.
	 * <p>
	 * By default returns an empty string.
	 */
	@NonNull default String getName() { return ""; }

	/**
	 * Returns a graph of node connections based on the given set of nodes.
	 *
	 * @param identities node identities.
	 *
	 * @return a directed graph of node connections.
	 */
	@NonNull DirectedGraph<String, DefaultEdge> getGraph(@NonNull Set<NodeIdentity> identities);
}
