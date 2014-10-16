package org.age.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.age.console.command.Command;
import org.age.console.command.Main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.collect.Iterables;
import jline.console.ConsoleReader;

public class Console {

	private static final Logger log = LoggerFactory.getLogger(Console.class);

	private final ConsoleReader reader;

	private final PrintWriter printWriter;

	@Inject private List<Command> commands;

	public Console() throws IOException {
		reader = new ConsoleReader();
		reader.setPrompt("AgE> ");

		printWriter = new PrintWriter(reader.getOutput());
	}

	public void mainLoop() throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			try {
				final Main main = new Main();
				final JCommander mainCommander = new JCommander(main);
				commands.forEach(mainCommander::addCommand);

				mainCommander.parse(line.split("\\s"));
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
