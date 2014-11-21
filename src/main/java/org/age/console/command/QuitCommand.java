/*
 * Created: 2014-10-07
 * $Id$
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
public class QuitCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(QuitCommand.class);

	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) {
		log.debug("Quit command called.");
		return false;
	}


	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
