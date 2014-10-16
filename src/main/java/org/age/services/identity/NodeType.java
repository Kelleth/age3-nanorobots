package org.age.services.identity;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum NodeType {

	UNKNOWN("unknown"),
	SATELLITE("satellite"),
	COMPUTE("compute");

	private final String name;

	NodeType(@NonNull final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
