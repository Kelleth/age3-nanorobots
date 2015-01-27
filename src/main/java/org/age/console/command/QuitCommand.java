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
 * Created: 2014-10-07.
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import javax.inject.Named;

/**
 * Command for quiting the console.
 */
@Named
@Parameters(commandNames = {"quit", "exit"}, commandDescription = "Quit the console")
public final class QuitCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(QuitCommand.class);

	@Override
	public void execute(final @NonNull JCommander commander, final @NonNull ConsoleReader reader,
	                    final @NonNull PrintWriter printWriter) {
		log.debug("Quit command called.");
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
