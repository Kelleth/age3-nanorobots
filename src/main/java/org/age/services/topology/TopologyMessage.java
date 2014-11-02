/*
 * Created: 2014-09-18
 * $Id$
 */

package org.age.services.topology;

import java.io.Serializable;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

@Immutable
public class TopologyMessage implements Serializable {

	public enum Type {
		MASTER_ELECTED(false),
		TOPOLOGY_SELECTED(false);

		private final boolean payloadRequired;

		Type(final boolean payloadRequired) {
			this.payloadRequired = payloadRequired;
		}

		public boolean isPayloadRequired() {
			return payloadRequired;
		}
	}

	private static final long serialVersionUID = -5867847961864763792L;

	private final Type type;

	private final Serializable payload;

	TopologyMessage(@NonNull final Type type, @Nullable final Serializable payload) {
		this.type = requireNonNull(type);
		this.payload = payload;
	}

	@NonNull public static TopologyMessage createWithoutPayload(@NonNull final Type type) {
		checkArgument(!type.isPayloadRequired(), "Message type require payload.");
		return new TopologyMessage(type, null);
	}

	@NonNull public Type getType() {
		return type;
	}

	public boolean hasType(@NonNull final Type typeToCheck) {
		return type == requireNonNull(typeToCheck);
	}

	@NonNull public Optional<@Nullable Serializable> getPayload() {
		return Optional.ofNullable(payload);
	}

	@Override public String toString() {
		return toStringHelper(this).add("type", type).addValue(payload).toString();
	}
}
