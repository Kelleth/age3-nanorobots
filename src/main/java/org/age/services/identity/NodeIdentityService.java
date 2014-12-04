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
