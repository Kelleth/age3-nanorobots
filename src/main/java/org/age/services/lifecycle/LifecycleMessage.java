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
 * Created: 2014-09-18
 */

package org.age.services.lifecycle;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Optional;

@Immutable
public class LifecycleMessage implements Serializable {

	public enum Type {
		DESTROY(false);

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

	LifecycleMessage(@NonNull final Type type, @Nullable final Serializable payload) {
		this.type = requireNonNull(type);
		this.payload = payload;
	}

	public static @NonNull LifecycleMessage createWithoutPayload(@NonNull final Type type) {
		checkArgument(!type.isPayloadRequired(), "Message type require payload.");
		return new LifecycleMessage(type, null);
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
