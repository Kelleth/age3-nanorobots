/*
 * Created: 2014-10-07
 * $Id$
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

