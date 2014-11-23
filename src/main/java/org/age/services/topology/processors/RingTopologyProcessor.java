/*
 * Created: 2014-08-28
 */

package org.age.services.topology.processors;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Objects.requireNonNull;

import org.age.services.identity.NodeDescriptor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

/**
 * Ring topology generator.
 * <p>
 * Sample topologies:
 * <ul>
 * <li> for one node: [(1,1)]
 * <li> for two nodes: [(2,1), (1,2)]
 * <li> for three nodes: (3,1), (1,2), (2,3)
 * </ul>
 */
@Named
public final class RingTopologyProcessor implements TopologyProcessor {

	@Override public @NonNull String name() {
		return "ring";
	}

	@Override public @NonNull DirectedGraph<String, DefaultEdge> createGraphFrom(
			final @NonNull Set<? extends NodeDescriptor> identities) {
		requireNonNull(identities);

		final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		identities.forEach(identity -> graph.addVertex(identity.id()));

		final List<String> sortedIds = identities.stream()
		                                         .map(NodeDescriptor::id)
		                                         .sorted()
		                                         .collect(Collectors.toList());
		sortedIds.stream().reduce(getLast(sortedIds), (nodeIdentity1, nodeIdentity2) -> {
			graph.addEdge(nodeIdentity1, nodeIdentity2);
			return nodeIdentity2;
		});
		return new UnmodifiableDirectedGraph<>(graph);
	}

	@Override public String toString() {
		return name();
	}
}
