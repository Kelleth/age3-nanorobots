/*
 * Created: 2014-08-22
 */

package org.age.services.identity.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import org.age.services.identity.NodeType;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.Set;

/**
 * Default node descriptor.
 */
@Immutable
public final class NodeDescriptor implements org.age.services.identity.NodeDescriptor {

	private static final long serialVersionUID = -4461499899468219523L;

	private final String id;

	private final NodeType type;

	private final ImmutableSet<@NonNull String> services;

	public NodeDescriptor(final @NonNull String id, final @NonNull NodeType type,
	                      final @NonNull Set<@NonNull String> services) {
		this.id = requireNonNull(id);
		this.type = requireNonNull(type);
		this.services = ImmutableSet.copyOf(requireNonNull(services));
	}

	@Override public @NonNull Set<@NonNull String> services() {
		return services;
	}

	@Override public @NonNull String id() {
		return id;
	}

	@Override public @NonNull NodeType type() {
		return type;
	}

	@Override public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NodeDescriptor)) {
			return false;
		}
		final NodeDescriptor other = (NodeDescriptor)obj;
		return Objects.equals(id, other.id) && Objects.equals(type, other.type);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(id).add("type", type).toString();
	}
}
