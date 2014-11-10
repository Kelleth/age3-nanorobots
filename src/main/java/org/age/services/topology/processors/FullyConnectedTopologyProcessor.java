/*
 * Created: 2014-08-28
 * $Id$
 */

package org.age.services.topology.processors;

import java.util.Set;

import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import org.age.services.identity.NodeIdentity;

import static com.google.common.collect.Sets.cartesianProduct;

@Named
public final class FullyConnectedTopologyProcessor implements TopologyProcessor {

	private static final int PRIORITY = 50;

	@Override public int getPriority() {
		return PRIORITY;
	}

	@NonNull
	@Override
	public String getName() {
		return "fully connected";
	}

	@NonNull
	@Override
	public DirectedGraph<String, DefaultEdge> getGraph(@NonNull final Set<NodeIdentity> identities) {
		final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		identities.forEach(identity -> graph.addVertex(identity.id()));
		cartesianProduct(identities, identities).forEach(elem -> {
			final NodeIdentity id1 = elem.get(0);
			final NodeIdentity id2 = elem.get(1);
			if (!id1.equals(id2)) {
				graph.addEdge(id1.id(), id2.id());
			}
		});
		return new UnmodifiableDirectedGraph<>(graph);
	}

	@Override public String toString() {
		return getName();
	}
}