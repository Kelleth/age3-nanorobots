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

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Immutable
public final class SpringConfiguration implements WorkerConfiguration {

	private static final long serialVersionUID = 4719974331488707814L;

	private final String pathToFile;

	public SpringConfiguration(final @NonNull String pathToFile) throws FileNotFoundException {
		requireNonNull(pathToFile);

		final Path path = Paths.get(pathToFile);
		if (!Files.exists(path)) {
			throw new FileNotFoundException("Configuration file " + pathToFile + " does not exist.");
		}

		this.pathToFile = path.normalize().toString();
	}

	@Override public @NonNull TaskBuilder taskBuilder() {
		return TaskBuilder.fromConfig(pathToFile);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(pathToFile).toString();
	}
}
