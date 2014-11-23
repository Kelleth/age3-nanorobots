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
