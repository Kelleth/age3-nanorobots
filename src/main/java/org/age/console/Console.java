package org.age.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import org.age.console.command.Command;
import org.age.console.command.Main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Iterables;
import jline.console.ConsoleReader;

/**
 * Console is the shell-like interface for managing the cluster and AgE nodes.
 * </p>
 * A command is a class that implements the {@link org.age.console.command.Command} interface
 * and is annotated using the {@link com.beust.jcommander.Parameters} and {@link javax.inject.Named} annotations.
 * Such commands are automatically recognized and available for the user.
 */
public class Console {

	private static final Logger log = LoggerFactory.getLogger(Console.class);

	private static final Pattern WHITESPACE = Pattern.compile("\\s");

	private final ConsoleReader reader;

	private final PrintWriter printWriter;

	@Inject private ApplicationContext applicationContext;

	public Console() throws IOException {
		reader = new ConsoleReader();
		reader.setPrompt("AgE> ");

		printWriter = new PrintWriter(reader.getOutput());
	}

	public void mainLoop() throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				// We need to allocate new instances every time
				final Main main = new Main();
				final JCommander mainCommander = new JCommander(main);
				final Collection<Command> commands = applicationContext.getBeansOfType(Command.class).values();
				commands.forEach(mainCommander::addCommand);

				mainCommander.parse(WHITESPACE.split(line));
				final String parsedCommand = mainCommander.getParsedCommand();

				final Command command;
				final JCommander commander;
				if (parsedCommand == null) {
					commander = mainCommander;
					command = main;
				} else {
					commander = mainCommander.getCommands().get(parsedCommand);
					command = (Command)Iterables.getOnlyElement(commander.getObjects());
				}
				if (!command.execute(commander, reader, printWriter)) {
					break;
				}
			} catch (final ParameterException e) {
				printWriter.println("Incorrect command." + e.getMessage());
			}
		}
	}
}
