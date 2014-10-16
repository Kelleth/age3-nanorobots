package org.age.services.topology;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import org.age.services.identity.NodeIdentity;

public interface TopologyProcessor {

	default int getPriority() {
		return 0;
	}

	@NonNull String getName();

	@NonNull DirectedGraph<String, DefaultEdge> getGraph(@NonNull Set<NodeIdentity> identities);
}
