package org.age.console;

import static java.util.Objects.isNull;

import org.age.console.command.Command;
import org.age.console.command.MainCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Iterables;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Console is the shell-like interface for managing the cluster and AgE nodes.
 *
 * A command is a class that implements the {@link org.age.console.command.Command} interface
 * and is annotated using the {@link com.beust.jcommander.Parameters} and {@link javax.inject.Named} annotations.
 * Such commands are automatically recognized and available for the user.
 */
public final class Console {

	private static final Pattern WHITESPACE = Pattern.compile("\\s");

	private static final Logger log = LoggerFactory.getLogger(Console.class);

	private final ConsoleReader reader = new ConsoleReader();

	private final @MonotonicNonNull PrintWriter printWriter;

	@Inject private @MonotonicNonNull ApplicationContext applicationContext;

	@Inject private @MonotonicNonNull CommandCompleter commandCompleter;

	public Console() throws IOException {
		reader.setPrompt("AgE> ");

		printWriter = new PrintWriter(reader.getOutput());
	}

	@PostConstruct private void construct() {
		reader.addCompleter(commandCompleter);
	}

	public void mainLoop() throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			log.debug("Read command: {}.", line);
			try {
				// We need to allocate new instances every time
				final MainCommand mainCommand = new MainCommand();
				final JCommander mainCommander = new JCommander(mainCommand);
				final Collection<Command> commands = applicationContext.getBeansOfType(Command.class).values();
				commands.forEach(mainCommander::addCommand);

				mainCommander.parse(WHITESPACE.split(line));
				final String parsedCommand = mainCommander.getParsedCommand();

				final Command command;
				final JCommander commander;
				if (isNull(parsedCommand)) {
					commander = mainCommander;
					command = mainCommand;
				} else {
					commander = mainCommander.getCommands().get(parsedCommand);
					command = (Command)Iterables.getOnlyElement(commander.getObjects());
				}
				if (!command.execute(commander, reader, printWriter)) {
					break;
				}
			} catch (final ParameterException e) {
				log.error("Incorrect command.", e);
				printWriter.println("Incorrect command." + e.getMessage());
			}
		}
	}
}
