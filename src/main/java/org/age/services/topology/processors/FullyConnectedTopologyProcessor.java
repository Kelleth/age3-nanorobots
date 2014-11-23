/*
 * Created: 2014-08-28
 */

package org.age.services.topology.processors;

import static com.google.common.collect.Sets.cartesianProduct;
import static java.util.Objects.requireNonNull;

import org.age.services.identity.NodeDescriptor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import java.util.Set;

import javax.inject.Named;

@Named
public final class FullyConnectedTopologyProcessor implements TopologyProcessor {

	private static final int PRIORITY = 50;

	@Override public int priority() {
		return PRIORITY;
	}

	@Override public @NonNull String name() {
		return "fully connected";
	}


	@Override public @NonNull DirectedGraph<String, DefaultEdge> createGraphFrom(
			final @NonNull Set<? extends NodeDescriptor> identities) {
		requireNonNull(identities);

		final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		identities.forEach(identity -> graph.addVertex(identity.id()));
		cartesianProduct(identities, identities).forEach(elem -> {
			final NodeDescriptor id1 = elem.get(0);
			final NodeDescriptor id2 = elem.get(1);
			if (!id1.equals(id2)) {
				graph.addEdge(id1.id(), id2.id());
			}
		});
		return new UnmodifiableDirectedGraph<>(graph);
	}

	@Override public String toString() {
		return name();
	}
}
