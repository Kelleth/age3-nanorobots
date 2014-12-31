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
/*
 * Created: 2014-10-16
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Maps.newHashMap;

import org.age.annotation.ForTestsOnly;
import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting info about the local node.
 */
@Named
@Scope("prototype")
@Parameters(commandNames = "local", commandDescription = "Local node management", optionPrefixes = "--")
public class LocalCommand implements Command {

	private enum Operation {
		INFO("info");

		private final String operationName;

		Operation(final @NonNull String operationName) {
			this.operationName = operationName;
		}

		public String operationName() {
			return operationName;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(LocalCommand.class);

	private final Map<String, Consumer<@NonNull PrintWriter>> handlers = newHashMap();

	@Inject private @NonNull NodeIdentityService identityService;


	@Parameter private @MonotonicNonNull List<String> unnamed;

	public LocalCommand() {
		handlers.put(Operation.INFO.operationName(), this::info);
	}

	@Override public final @NonNull Set<String> operations() {
		return Arrays.stream(Operation.values()).map(Operation::operationName).collect(Collectors.toSet());
	}

	@Override
	public void execute(final @NonNull JCommander commander, final @NonNull ConsoleReader reader,
	                    final @NonNull PrintWriter printWriter) {
		final String command = getOnlyElement(unnamed, "");
		if (!handlers.containsKey(command)) {
			printWriter.println("Unknown command " + command);
			return;
		}
		handlers.get(command).accept(printWriter);
	}

	private void info(final @NonNull PrintWriter printWriter) {
		final NodeDescriptor identity = identityService.descriptor();
		printWriter.println("Local node info = {");
		printWriter.println("\tid = " + identity.id());
		printWriter.println("\ttype = " + identity.type());
		printWriter.println("\tservices = " + identity.services());
		printWriter.println("}");
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}

	@ForTestsOnly void setUnnamed(final @NonNull List<String> unnamed) {
		this.unnamed = unnamed;
	}
}
