/*
 * Copyright (C) 2014 Intelligent Information Systems Group.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.age.console;

import static java.util.Objects.isNull;

import org.age.console.command.Command;
import org.age.console.command.HelpCommand;
import org.age.console.command.MainCommand;
import org.age.console.command.QuitCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
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

	private final PrintWriter printWriter;

	@Inject private @NonNull ApplicationContext applicationContext;

	@Inject private @NonNull CommandCompleter commandCompleter;

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
				// Because of limitations of JCommander, we need to do this using instanceof
				if (command instanceof QuitCommand) {
					break;
				}
				if (command instanceof HelpCommand) {
					mainCommander.usage();
				}
				command.execute(commander, reader, printWriter);
			} catch (final ParameterException e) {
				log.error("Incorrect command.", e);
				printWriter.println("Incorrect command. " + e.getMessage());
			}
		}
	}
}
