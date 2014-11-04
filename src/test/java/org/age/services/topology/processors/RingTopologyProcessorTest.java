package org.age.services.topology.processors;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeIdentity;

import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.toMap;

public class RingTopologyProcessorTest {

	private RingTopologyProcessor processor;

	@BeforeMethod public void setUp() {
		processor = new RingTopologyProcessor();
	}

	@AfterMethod public void tearDown() {
		processor = null;
	}

	@Test public void testSingleNode() {
		final String nodeId = "1";
		final NodeIdentity nodeIdentity = mock(NodeIdentity.class);
		when(nodeIdentity.getId()).thenReturn(nodeId);
		final ImmutableSet<NodeIdentity> identities = ImmutableSet.of(nodeIdentity);

		final DirectedGraph<String, DefaultEdge> graph = processor.getGraph(identities);

		assertThat(graph.containsVertex(nodeId), is(true));
		assertThat(graph.inDegreeOf(nodeId), is(equalTo(1)));
		assertThat(graph.outDegreeOf(nodeId), is(equalTo(1)));
		assertThat(graph.getEdge(nodeId, nodeId), is(notNullValue()));
	}

	@Test public void testTwoNodes() {
		final String node1Id = "1";
		final String node2Id = "2";
		final NodeIdentity node1Identity = mock(NodeIdentity.class);
		final NodeIdentity node2Identity = mock(NodeIdentity.class);
		when(node1Identity.getId()).thenReturn(node1Id);
		when(node2Identity.getId()).thenReturn(node2Id);
		final ImmutableSet<NodeIdentity> identities = ImmutableSet.of(node1Identity, node2Identity);

		final DirectedGraph<String, DefaultEdge> graph = processor.getGraph(identities);

		assertThat(graph.containsVertex(node1Id), is(true));
		assertThat(graph.inDegreeOf(node1Id), is(equalTo(1)));
		assertThat(graph.outDegreeOf(node1Id), is(equalTo(1)));

		assertThat(graph.containsVertex(node2Id), is(true));
		assertThat(graph.inDegreeOf(node2Id), is(equalTo(1)));
		assertThat(graph.outDegreeOf(node2Id), is(equalTo(1)));

		assertThat(graph.getEdge(node1Id, node1Id), is(nullValue()));
		assertThat(graph.getEdge(node1Id, node2Id), is(notNullValue()));
		assertThat(graph.getEdge(node2Id, node1Id), is(notNullValue()));
		assertThat(graph.getEdge(node2Id, node2Id), is(nullValue()));
	}

	@Test public void testThreeNodes() {
		final Map<String, NodeIdentity> map = toMap(ImmutableSet.of("1", "2", "3"),
		                                                                                input -> mock(
				                                                                                NodeIdentity.class));

		map.forEach((s, nodeIdentity) -> when(nodeIdentity.getId()).thenReturn(s));

		final Set<NodeIdentity> identities = ImmutableSet.copyOf(map.values());
		final DirectedGraph<String, DefaultEdge> graph = processor.getGraph(identities);

		map.keySet().forEach(nodeId -> {
			assertThat(graph.containsVertex(nodeId), is(true));
			assertThat(graph.inDegreeOf(nodeId), is(equalTo(1)));
			assertThat(graph.outDegreeOf(nodeId), is(equalTo(1)));
			assertThat(graph.getEdge(nodeId, nodeId), is(nullValue()));
		});

		final String start = map.keySet().stream().findAny().get();
		String current = start;
		int counter = 0;

		do {
			final Set<DefaultEdge> outEdges = graph.outgoingEdgesOf(current);
			assertThat(outEdges, hasSize(1));
			current = graph.getEdgeTarget(getOnlyElement(outEdges));
			counter++;
		} while (!Objects.equals(current, start));

		assertThat(counter, is(equalTo(3)));
	}
}
