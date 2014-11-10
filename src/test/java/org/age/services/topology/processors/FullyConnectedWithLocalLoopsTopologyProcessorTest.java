package org.age.services.topology.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeIdentity;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public final class FullyConnectedWithLocalLoopsTopologyProcessorTest {

	@Nullable private FullyConnectedWithLocalLoopsTopologyProcessor processor;

	@BeforeMethod public void setUp() {
		processor = new FullyConnectedWithLocalLoopsTopologyProcessor();
	}

	@AfterMethod public void tearDown() {
		processor = null;
	}

	@Test public void testSingleNode() {
		final String nodeId = "1";
		final NodeIdentity nodeIdentity = mock(NodeIdentity.class);
		when(nodeIdentity.id()).thenReturn(nodeId);
		final ImmutableSet<NodeIdentity> identities = ImmutableSet.of(nodeIdentity);

		final DirectedGraph<String, DefaultEdge> graph = processor.getGraph(identities);

		assertThat(graph.containsVertex(nodeId)).isTrue();
		assertThat(graph.inDegreeOf(nodeId)).isEqualTo(1);
		assertThat(graph.outDegreeOf(nodeId)).isEqualTo(1);
		assertThat(graph.getEdge(nodeId, nodeId)).isNotNull();
	}

	@Test public void testTwoNodes() {
		final String node1Id = "1";
		final String node2Id = "2";
		final NodeIdentity node1Identity = mock(NodeIdentity.class);
		final NodeIdentity node2Identity = mock(NodeIdentity.class);
		when(node1Identity.id()).thenReturn(node1Id);
		when(node2Identity.id()).thenReturn(node2Id);
		final ImmutableSet<NodeIdentity> identities = ImmutableSet.of(node1Identity, node2Identity);

		final DirectedGraph<String, DefaultEdge> graph = processor.getGraph(identities);

		assertThat(graph.containsVertex(node1Id)).isTrue();
		assertThat(graph.inDegreeOf(node1Id)).isEqualTo(2);
		assertThat(graph.outDegreeOf(node1Id)).isEqualTo(2);

		assertThat(graph.containsVertex(node2Id)).isTrue();
		assertThat(graph.inDegreeOf(node2Id)).isEqualTo(2);
		assertThat(graph.outDegreeOf(node2Id)).isEqualTo(2);

		assertThat(graph.getEdge(node1Id, node1Id)).isNotNull();
		assertThat(graph.getEdge(node1Id, node2Id)).isNotNull();
		assertThat(graph.getEdge(node2Id, node1Id)).isNotNull();
		assertThat(graph.getEdge(node2Id, node2Id)).isNotNull();
	}
}
