/*
 * Created: 2014-08-28
 * $Id$
 */

package org.age.services.topology.processors;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import org.age.services.identity.NodeIdentity;

import static com.google.common.collect.Iterables.getLast;

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

	@NonNull
	@Override
	public String getName() {
		return "ring";
	}

	@NonNull
	@Override
	public DirectedGraph<String, DefaultEdge> getGraph(@NonNull final Set<NodeIdentity> identities) {
		final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		identities.forEach(identity -> graph.addVertex(identity.id()));

		final List<String> sortedIds = identities.stream().map(NodeIdentity::id).sorted().collect(Collectors.toList());
		sortedIds.stream().reduce(getLast(sortedIds), (nodeIdentity1, nodeIdentity2) -> {
			graph.addEdge(nodeIdentity1, nodeIdentity2);
			return nodeIdentity2;
		});
		return new UnmodifiableDirectedGraph<>(graph);
	}

	@Override public String toString() {
		return getName();
	}
}
