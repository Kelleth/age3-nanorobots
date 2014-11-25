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
