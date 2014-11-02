/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.console.command;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import jline.console.ConsoleReader;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Command providing clearing of the screen.
 */
@Named
@Parameters(commandNames = "clear", commandDescription = "Clear screen")
public class Clear implements Command {

	private static final Logger log = LoggerFactory.getLogger(Clear.class);

	@Override
	public boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter) {
		try {
			reader.clearScreen();
		} catch (final IOException e) {
			log.error("Reader exception.", e);
		}
		return true;
	}


	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
