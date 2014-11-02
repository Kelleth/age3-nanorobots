/*
 * Created: 2014-10-09
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;

import com.beust.jcommander.JCommander;
import jline.console.ConsoleReader;

/**
 * Interface for commands used by the console.
 * <p/>
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
	boolean execute(JCommander commander, ConsoleReader reader, PrintWriter printWriter);
}
