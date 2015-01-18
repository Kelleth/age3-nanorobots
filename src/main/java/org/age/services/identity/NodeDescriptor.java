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
 * Created: 2014-11-22.
 */

package org.age.services.identity;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Set;

/**
 * Wraps cached node identity information in a single, serializable object.
 */
@Immutable
public interface NodeDescriptor extends Serializable {

	/**
	 * Returns the ID of the node described in this descriptor.
	 */
	@NonNull String id();

	/**
	 * Returns the type of the node described in this descriptor.
	 */
	@NonNull NodeType type();

	/**
	 * Returns the set of services running on the node described by this descriptor.
	 */
	@NonNull Set<@NonNull String> services();

}
