package org.age.console;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.union;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

public final class CommandCompleterTest {

	private final Set<String> commands = ImmutableSet.of("command1", "another", "command2", "different", "three", "test");

	private final Set<String> mainParameters = ImmutableSet.of("--param", "--qwerty", "--min", "--popular");

	private final Set<String> commandParameters = ImmutableSet.of("--paaaa", "--pb", "--another", "--other");

	private final Set<String> testCommandParameters = ImmutableSet.of("--list-examples", "--example", "--config");

	@Mock private CommandIntrospector introspector;

	@InjectMocks private CommandCompleter commandCompleter;

	@BeforeMethod public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(introspector.allCommands()).thenReturn(commands);
		when(introspector.parametersOfMainCommand()).thenReturn(mainParameters);
		when(introspector.commandsStartingWith(anyString())).thenAnswer(invocation -> {
			final String prefix = invocation.getArgumentAt(0, String.class);
			return commands.stream().filter(s -> s.startsWith(prefix)).collect(toSet());
		});
		when(introspector.parametersOfCommand("another")).thenReturn(commandParameters);
		when(introspector.parametersOfCommandStartingWith(eq("another"), anyString())).thenAnswer(invocation -> {
			final String prefix = invocation.getArgumentAt(1, String.class);
			return commandParameters.stream().filter(s -> s.startsWith(prefix)).collect(toSet());
		});
		when(introspector.parametersOfCommandStartingWith(eq("test"), anyString())).thenAnswer(invocation -> {
			final String prefix = invocation.getArgumentAt(1, String.class);
			return testCommandParameters.stream().filter(s -> s.startsWith(prefix)).collect(toSet());
		});
	}

	@Test public void testEmptyBuffer() {
		final List<CharSequence> candidates = newArrayList();
		final int position = commandCompleter.complete("", 0, candidates);

		assertThat(position).isEqualTo(0);
		assertThat(candidates).containsOnlyElementsOf(union(commands, mainParameters));
	}

	@Test public void testEmptyBufferWithSpaces() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "    ";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(buffer.length());
		assertThat(candidates).containsOnlyElementsOf(union(commands, mainParameters));
	}

	@Test public void testBeginningOfCommand_singleMatch() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "anot";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(0);
		assertThat(candidates).containsOnly("another");
	}

	@Test public void testBeginningOfCommand_multipleMatches() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "co";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(0);
		assertThat(candidates).containsOnlyElementsOf(
				commands.stream().filter(s -> s.startsWith("co")).collect(toSet()));
	}

	@Test public void testInsideCommand_singleMatch() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "anda";
		final int position = commandCompleter.complete(buffer, 2, candidates);

		assertThat(position).isEqualTo(0);
		assertThat(candidates).containsOnly("another");
	}

	@Test public void testInsideCommand_multipleMatches() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "comrade";
		final int position = commandCompleter.complete(buffer, 3, candidates);

		assertThat(position).isEqualTo(0);
		assertThat(candidates).containsOnlyElementsOf(
				commands.stream().filter(s -> s.startsWith("co")).collect(toSet()));
	}

	@Test public void testParametersOfCommand_withDash() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "another -";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(buffer.length() - 1);
		assertThat(candidates).containsOnlyElementsOf(commandParameters);
	}

	@Test public void testParametersOfCommand_withTwoDashes() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "another --";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(buffer.length() - 2);
		assertThat(candidates).containsOnlyElementsOf(commandParameters);
	}

	@Test public void testParametersOfCommand_noPrefix() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "another --b ";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(buffer.length());
		assertThat(candidates).containsOnlyElementsOf(commandParameters);
	}

	@Test public void testParametersOfCommand_withFragmentOfParameter() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "another --p";
		final int position = commandCompleter.complete(buffer, buffer.length(), candidates);

		assertThat(position).isEqualTo(buffer.length() - 3);
		assertThat(candidates).containsOnlyElementsOf(commandParameters.stream().filter(s -> s.startsWith("--p")).collect(toSet()));
	}

	@Test public void testParametersOfTestCommand_withFragmentOfParameter() {
		final List<CharSequence> candidates = newArrayList();
		final String buffer = "test --l";
		final int position = commandCompleter.complete(buffer, 8, candidates);

		assertThat(position).isEqualTo(buffer.length() - 3);
		assertThat(candidates).containsOnlyElementsOf(testCommandParameters.stream().filter(s -> s.startsWith("--l")).collect(toSet()));
	}
}