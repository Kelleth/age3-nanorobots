/*
 * Created: 2014-08-22
 * $Id$
 */

package org.age.services.identity;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.google.common.base.MoreObjects.toStringHelper;

@Immutable
public class NodeIdentity implements Serializable {

	private static final long serialVersionUID = -4461499899468219523L;

	private final String id;
	private final NodeType type;
	private final Set<@NonNull String> services;

	public NodeIdentity(@NonNull final String id, @NonNull final NodeType type,
	                    @NonNull final Set<@NonNull String> services) {
		this.id = requireNonNull(id);
		this.type = requireNonNull(type);
		this.services = requireNonNull(services);
	}

	public Set<@NonNull String> getServices() {
		return services;
	}

	@NonNull
	public String getId() {
		return id;
	}

	@NonNull
	public NodeType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.type);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NodeIdentity)) {
			return false;
		}
		final NodeIdentity other = (NodeIdentity)obj;
		if (this == other) {
			return true;
		}
		return false;//Objects.equals(this.id, other.id) && Objects.equals(this.type, other.type);
	}

	@Override
	public String toString() {
		return toStringHelper(this).addValue(id).add("type", type).toString();
	}
}
