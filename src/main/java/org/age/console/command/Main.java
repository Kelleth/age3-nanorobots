/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jline.console.ConsoleReader;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Main (empty) command for the console. Provides help for the user.
 */
@Parameters(optionPrefixes = "--")
public class Main implements Command {

	@Parameter(names = "--help", help = true) private boolean help;

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}

	@Override
	public boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter) {
		if (help) {
			commander.usage();
		}
		return true;
	}
}

