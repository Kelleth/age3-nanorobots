/*
 * Created: 2014-10-09
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;

import com.beust.jcommander.JCommander;
import jline.console.ConsoleReader;

public interface Command {
	boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter);
}
