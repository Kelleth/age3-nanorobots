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
 * Created: 2014-10-07
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * Main (empty) command for the console. Provides help for the user.
 */
@Parameters(optionPrefixes = "--")
public class MainCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(MainCommand.class);

	@Parameter(names = "--help", help = true) private boolean help;

	@Override public String toString() {
		return toStringHelper(this).toString();
	}

	@Override public boolean execute(final JCommander commander, final ConsoleReader reader,
	                                 final PrintWriter printWriter) {
		if (help) {
			commander.usage();
		}
		return true;
	}
}

