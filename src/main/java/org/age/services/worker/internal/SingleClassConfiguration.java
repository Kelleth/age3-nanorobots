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
 * Created: 2015-01-17.
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

@Immutable
public final class SingleClassConfiguration implements WorkerConfiguration {

	private static final long serialVersionUID = 1113065883705198832L;

	private final String className;


	public SingleClassConfiguration(final @NonNull String className) {
		this.className = requireNonNull(className);
	}

	@Override public @NonNull TaskBuilder taskBuilder() {
		return TaskBuilder.fromClass(className);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(className).toString();
	}
}
