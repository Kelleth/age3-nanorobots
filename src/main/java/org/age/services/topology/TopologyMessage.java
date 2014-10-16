/*
 * Created: 2014-09-18
 * $Id$
 */

package org.age.services.topology;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.NonNull;

public class TopologyMessage implements Serializable {
	public enum Type {
		TOPOLOGY_SELECTED
	}
	private final Type type;

	public TopologyMessage(@NonNull final Type messageType) {
		this.type = messageType;
	}

	@NonNull
	public Type getType() {
		return type;
	}
}
