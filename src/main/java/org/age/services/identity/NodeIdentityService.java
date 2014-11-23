/*
 * Created by nnidyu on 22.11.14.
 */

package org.age.services.identity;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

/**
 * Node identity service is a local service that provides retrospection about the node and defines its identity.
 */
public interface NodeIdentityService {
	/**
	 * Returns the stringified ID of the node.
	 */
	@NonNull String nodeId();

	/**
	 * Returns the type of the node.
	 *
	 * @see org.age.services.identity.NodeType
	 */
	@NonNull NodeType nodeType();

	/**
	 * Returns the descriptor for the node that contains cached, serializable information from the service.
	 */
	@NonNull NodeDescriptor descriptor();

	/**
	 * Returns the set of running services.
	 */
	@NonNull Set<@NonNull String> services();

	/**
	 * Tells whether the node is compute node.
	 *
	 * A node is compute when it has {@link #nodeType()} equal to {@link NodeType#COMPUTE}.
	 */
	boolean isCompute();

	/**
	 * Checks whether node has a given type.
	 *
	 * @param type a type to check.
	 */
	boolean is(@NonNull NodeType type);
}
