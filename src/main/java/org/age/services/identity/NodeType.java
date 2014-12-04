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

package org.age.services.identity;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Node type describes what kind of node is being run.
 */
public enum NodeType {

	/**
	 * Unknown type of node - usually means that there is an error.
	 */
	UNKNOWN("unknown"),
	/**
	 * Satellite node - it does not participate in computation (e.g. console node).
	 */
	SATELLITE("satellite"),
	/**
	 * Compute node - that participates in computation and has {@link org.age.services.worker.WorkerService} running.
	 */
	COMPUTE("compute");

	private final String name;

	NodeType(@NonNull final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
