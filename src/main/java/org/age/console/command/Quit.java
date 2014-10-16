/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;

import javax.inject.Named;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import jline.console.ConsoleReader;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
@Parameters(commandNames = {"quit", "exit"}, commandDescription = "Quit the console")
public class Quit implements Command {

	@Override
	public boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter) {
		return false;
	}


	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
