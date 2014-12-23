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
 * Created: 2014-10-09
 */

package org.age.console.command;

import static java.util.Collections.emptySet;

import com.beust.jcommander.JCommander;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Interface for commands used by the console.
 *
 * Each implementing class should be annotated using {@link com.beust.jcommander.Parameters} and {@link
 * com.beust.jcommander.Parameter} annotations.
 */
@FunctionalInterface
public interface Command {

	/**
	 * Main method of the command - called when the command is executed.
	 *
	 * @param commander
	 * 		Current (per command) {@link JCommander} instance.
	 * @param reader
	 * 		Current {@link ConsoleReader}.
	 * @param printWriter
	 * 		Current writer - command should use this writer for output.
	 *
	 * @return True if console should continue, false if the console should raise error or exit.
	 */
	boolean execute(@NonNull JCommander commander, @NonNull ConsoleReader reader, @NonNull PrintWriter printWriter)
			throws IOException;

	/**
	 * Returns set of suboperations (subcommands) of the command in the from of strings. By default returns an empty
	 * set.
	 *
	 * @return suboperations of the command.
	 */
	default Set<String> operations() {
		return emptySet();
	}
}
