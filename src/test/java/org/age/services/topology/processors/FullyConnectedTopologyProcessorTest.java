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

import static org.assertj.core.api.Assertions.assertThat;

import org.age.services.identity.NodeType;
import org.age.services.identity.internal.NodeDescriptor;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

public final class FullyConnectedTopologyProcessorTest {

	@Nullable private FullyConnectedTopologyProcessor processor;

	@BeforeMethod public void setUp() {
		processor = new FullyConnectedTopologyProcessor();
	}

	@AfterMethod public void tearDown() {
		processor = null;
	}

	@Test public void testSingleNode() {
		final String nodeId = "1";
		final NodeDescriptor nodeDescriptor = new NodeDescriptor(nodeId, NodeType.UNKNOWN, Collections.emptySet());
		final ImmutableSet<NodeDescriptor> identities = ImmutableSet.of(nodeDescriptor);

		final DirectedGraph<String, DefaultEdge> graph = processor.createGraphFrom(identities);

		assertThat(graph.containsVertex(nodeId)).isTrue();
		assertThat(graph.inDegreeOf(nodeId)).isEqualTo(0);
		assertThat(graph.outDegreeOf(nodeId)).isEqualTo(0);
		assertThat(graph.getEdge(nodeId, nodeId)).isNull();
	}

	@Test public void testTwoNodes() {
		final String node1Id = "1";
		final String node2Id = "2";
		final NodeDescriptor node1Identity = new NodeDescriptor(node1Id, NodeType.UNKNOWN, Collections.emptySet());
		final NodeDescriptor node2Identity = new NodeDescriptor(node2Id, NodeType.UNKNOWN, Collections.emptySet());
		final ImmutableSet<NodeDescriptor> identities = ImmutableSet.of(node1Identity, node2Identity);

		final DirectedGraph<String, DefaultEdge> graph = processor.createGraphFrom(identities);

		assertThat(graph.containsVertex(node1Id)).isTrue();
		assertThat(graph.inDegreeOf(node1Id)).isEqualTo(1);
		assertThat(graph.outDegreeOf(node1Id)).isEqualTo(1);

		assertThat(graph.containsVertex(node2Id)).isTrue();
		assertThat(graph.inDegreeOf(node2Id)).isEqualTo(1);
		assertThat(graph.outDegreeOf(node2Id)).isEqualTo(1);

		assertThat(graph.getEdge(node1Id, node1Id)).isNull();
		assertThat(graph.getEdge(node1Id, node2Id)).isNotNull();
		assertThat(graph.getEdge(node2Id, node1Id)).isNotNull();
		assertThat(graph.getEdge(node2Id, node2Id)).isNull();
	}
}
