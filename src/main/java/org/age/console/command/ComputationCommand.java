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
import static java.util.Objects.nonNull;

import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.lifecycle.LifecycleMessage;
import org.age.services.worker.WorkerMessage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting info about and managing the computation.
 */
@Named
@Scope("prototype")
@Parameters(commandNames = "computation", commandDescription = "Computation management", optionPrefixes = "--")
public class ComputationCommand implements Command {

	private enum Operation {
		LOAD("load"),
		INFO("info"),
		START("start"),
		STOP("stop");

		private final String operationName;

		Operation(final @NonNull String operationName) {
			this.operationName = operationName;
		}

		public String operationName() {
			return operationName;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(ComputationCommand.class);

	private final Map<String, Consumer<@NonNull PrintWriter>> handlers = newHashMap();

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject private @NonNull DiscoveryService discoveryService;

	@Parameter private List<String> unnamed;

	@Parameter(names = "--class") private String classToLoad;

	@Parameter(names = "--config") private String configToLoad;

	private @MonotonicNonNull ITopic<LifecycleMessage> lifecycleTopic;

	private @MonotonicNonNull ITopic<WorkerMessage<?>> workerTopic;

	public ComputationCommand() {
		handlers.put(Operation.LOAD.operationName(), this::load);
		handlers.put(Operation.INFO.operationName(), this::info);
		handlers.put(Operation.START.operationName(), this::start);
		handlers.put(Operation.STOP.operationName(), this::stop);
	}

	@PostConstruct private void construct() {
		lifecycleTopic = hazelcastInstance.getTopic("lifecycle/channel");
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

	@NonNull @Override public Set<String> operations() {
		return Arrays.stream(Operation.values()).map(Operation::operationName).collect(Collectors.toSet());
	}

	private void load(final @NonNull PrintWriter printWriter) {
		if (nonNull(classToLoad)) {
			log.debug("Loading class {}.", classToLoad);

			workerTopic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CLASS, classToLoad));
		} else if (nonNull(configToLoad)) {
			log.debug("Loading config from {}.", configToLoad);

			final Path path = Paths.get(configToLoad);
			if (!Files.exists(path)) {
				printWriter.println("File " + configToLoad + " does not exist.");
				return;
			}
			workerTopic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CONFIGURATION,
			                                                             path.normalize().toString()));
		} else {
			printWriter.println("No class or config to load.");
		}
	}

	private void info(final PrintWriter printWriter) {
		log.debug("Printing information about info.");
		final Set<NodeDescriptor> neighbours = discoveryService.allMembers();
		neighbours.forEach(printWriter::println);
	}

	private void start(final PrintWriter printWriter) {
		log.debug("Starting computation.");
		workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));
	}

	private void stop(final PrintWriter printWriter) {
		log.debug("Stopping computation.");
		workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.STOP_COMPUTATION));
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
