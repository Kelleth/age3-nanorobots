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
 * Created: 2014-11-23.
 */
package org.age.services.discovery;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

@Immutable
public class DiscoveryServiceStoppingEvent implements DiscoveryEvent {

	private final LocalDateTime timestamp = LocalDateTime.now();

	@Override public int hashCode() {
		return Objects.hash(timestamp);
	}

	@Override public boolean equals(final @Nullable Object obj) {
		if (!(obj instanceof DiscoveryServiceStoppingEvent)) {
			return false;
		}
		final DiscoveryServiceStoppingEvent other = (DiscoveryServiceStoppingEvent)obj;

		return Objects.equals(timestamp, other.timestamp);
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
