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
 * Created: 2015-02-10.
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import org.age.services.worker.internal.task.TaskBuilder;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

@Immutable
public final class SpringClasspathConfiguration implements WorkerConfiguration {

	private static final long serialVersionUID = 4719974331488707814L;

	private final String classpathLocation;

	public SpringClasspathConfiguration(final @NonNull String classpathLocation) {
		this.classpathLocation = requireNonNull(classpathLocation);
	}

	@Override public @NonNull TaskBuilder taskBuilder() {
		return TaskBuilder.fromClasspathConfig(classpathLocation);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(classpathLocation).toString();
	}
}
