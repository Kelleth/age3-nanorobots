/*
 * Created: 2014-10-07
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Named;

/**
 * Command providing an action of clearing the screen.
 */
@Named
@Parameters(commandNames = "clear", commandDescription = "Clear screen")
public class ClearScreenCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(ClearScreenCommand.class);

	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) throws IOException {
		log.debug("Clearing the screen.");
		reader.clearScreen();
		return true;
	}


	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
