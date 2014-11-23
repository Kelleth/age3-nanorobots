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
