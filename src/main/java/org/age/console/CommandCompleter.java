package org.age.console;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;

import jline.console.completer.Completer;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Auto-completer for the console.
 *
 * It autocompletes all loadable commands and their parameters.
 *
 * @see org.age.console.CommandIntrospector
 */
@Named
public class CommandCompleter implements Completer {

	private static final Pattern WHITESPACE = Pattern.compile("\\s");

	private static final char DASH = '-';

	private static final Logger log = LoggerFactory.getLogger(CommandCompleter.class);

	@Inject private @MonotonicNonNull CommandIntrospector introspector;

	@Override public int complete(final String buffer, final int cursor, final List<CharSequence> candidates) {
		requireNonNull(buffer);
		requireNonNull(candidates);

		log.debug("Command line to complete: {}; cursor: {}.", buffer, cursor);

		// Special case - empty (or whitespace only) buffer
		if (buffer.trim().isEmpty()) {
			candidates.addAll(introspector.allCommands());
			candidates.addAll(introspector.parametersOfMainCommand());
			return cursor;
		}

		final int lastPosition = cursor - 1;
		final String substring = buffer.substring(0, cursor);
		final String[] strings = WHITESPACE.split(substring);
		final String last = strings[strings.length - 1];
		final int returnPosition;

		if (introspector.allCommands().contains(strings[0])) {
			// Command in the beginning
			final String command = strings[0];
			// Fill with parameters
			if (buffer.charAt(lastPosition) == DASH) {
				log.debug("After dash.");
				candidates.addAll(introspector.parametersOfCommand(command));
				returnPosition = (buffer.charAt(lastPosition - 1) == DASH) ? (cursor - 2) : (cursor - 1);
			} else if (CharMatcher.WHITESPACE.matches(buffer.charAt(lastPosition))) {
				log.debug("Whitespace.");
				candidates.addAll(introspector.parametersOfCommand(command));
				returnPosition = cursor;
			} else {
				log.debug("Starting with: {}.", last);
				candidates.addAll(introspector.parametersOfCommandStartingWith(command, last));
				returnPosition = cursor - last.length();
			}
		} else {
			// No command in the beginning - fill with commands
			candidates.addAll(introspector.commandsStartingWith(last));
			returnPosition = cursor - last.length();
		}
		log.debug("Candidates: {}.", candidates);
		return returnPosition;
	}

	@Override public String toString() {
		return toStringHelper(this).add("introspector", introspector).toString();
	}
}
