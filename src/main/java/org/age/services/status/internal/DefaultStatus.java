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
 * Created: 2015-01-04.
 */

package org.age.services.status.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import org.age.services.status.Status;

import com.google.common.collect.ImmutableList;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Immutable
public final class DefaultStatus implements Status {

	private static final long serialVersionUID = -7617711578825244026L;

	private final LocalDateTime creationTimestamp = LocalDateTime.now();

	private final List<Throwable> errors;

	private DefaultStatus(final @NonNull ImmutableList<Throwable> errors) {
		assert nonNull(errors);

		this.errors = errors;
	}

	@Override public @NonNull LocalDateTime creationTimestamp() {
		return creationTimestamp;
	}

	@Override public @Immutable @NonNull List<Throwable> errors() {
		return errors;
	}

	@Override public int hashCode() {
		return Objects.hash(creationTimestamp);
	}

	@Override public boolean equals(final @Nullable Object obj) {
		if (!(obj instanceof DefaultStatus)) {
			return false;
		}
		final DefaultStatus other = (DefaultStatus)obj;

		return Objects.equals(creationTimestamp, other.creationTimestamp);
	}

	@Override public String toString() {
		return toStringHelper(this).add("createdAt", creationTimestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
		                           .add("errors", errors)
		                           .toString();
	}

	public static final class Builder {

		private @NonNull ImmutableList<Throwable> errors = ImmutableList.of();

		private Builder() {}

		public static Builder create() {
			return new Builder();
		}

		public Builder addErrors(final @NonNull List<Throwable> errors) {
			this.errors = ImmutableList.copyOf(requireNonNull(errors));
			return this;
		}

		public Status buildStatus() {
			return new DefaultStatus(errors);
		}

		@Override public String toString() {
			return toStringHelper(this).toString();
		}
	}
}
