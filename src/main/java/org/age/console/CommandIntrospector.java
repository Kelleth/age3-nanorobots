package org.age.console;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import org.age.console.command.Command;
import org.age.console.command.MainCommand;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Introspector that is able to provide information about available commands.
 */
@Named
public class CommandIntrospector {

	private static final Logger log = LoggerFactory.getLogger(CommandIntrospector.class);

	private final JCommander commander = new JCommander(new MainCommand());

	@Inject private @MonotonicNonNull Set<Command> commands;

	private @MonotonicNonNull Map<String, JCommander> commandsMap;

	@PostConstruct private void construct() {
		log.debug("Injected commands: {}.", commands);
		commands.forEach(commander::addCommand);
		commandsMap = commander.getCommands();
	}

	public @NonNull Set<String> allCommands() {
		return commandsMap.keySet();
	}

	public @NonNull Set<String> commandsStartingWith(final @NonNull String prefix) {
		requireNonNull(prefix);

		return commandsMap.keySet().stream().filter(s -> s.startsWith(prefix)).collect(toSet());
	}

	public @NonNull Set<String> parametersOfCommand(final @NonNull String command) {
		requireNonNull(command);
		checkArgument(!command.isEmpty());

		return commandsMap.get(command)
		                  .getParameters()
		                  .stream()
		                  .map(ParameterDescription::getLongestName)
		                  .collect(toSet());
	}

	public @NonNull Set<String> parametersOfCommandStartingWith(final @NonNull String command,
	                                                            final @NonNull String prefix) {
		requireNonNull(command);
		requireNonNull(prefix);
		checkArgument(!command.isEmpty());

		return commandsMap.get(command)
		                  .getParameters()
		                  .stream()
		                  .map(ParameterDescription::getLongestName)
		                  .filter(s -> s.startsWith(prefix))
		                  .collect(toSet());
	}

	public @NonNull Set<String> parametersOfMainCommand() {
		return commander.getParameters().stream().map(ParameterDescription::getLongestName).collect(toSet());
	}

	@Override public String toString() {
		return toStringHelper(this).add("commander", commander).toString();
	}
}
