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

import org.age.example.SimpleLongRunning;
import org.age.example.SimpleLongRunningWithError;
import org.age.services.lifecycle.LifecycleMessage;
import org.age.services.lifecycle.internal.DefaultNodeLifecycleService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.internal.DefaultWorkerService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command that eases testing the cluster.
 */
@Named
@Scope("prototype")
@Parameters(commandNames = "test", commandDescription = "Run sample computations", optionPrefixes = "--")
public class TestCommand implements Command {

	private enum Operation {
		LIST_EXAMPLES("list-examples"),
		EXECUTE("execute"),
		COMPUTATION_INTERRUPTED("computation-interrupted");

		private final String operationName;

		Operation(final @NonNull String operationName) {
			this.operationName = operationName;
		}

		public String operationName() {
			return operationName;
		}
	}

	private enum Type {
		DESTROY("destroy"),
		COMPUTE_ERROR("compute-error"),
		NODE_ERROR("node-error");

		private final String typeName;

		Type(final @NonNull String typeName) {
			this.typeName = typeName;
		}

		public String typeName() {
			return typeName;
		}
	}

	private static final String EXAMPLES_PACKAGE = "org.age.example";

	private static final Logger log = LoggerFactory.getLogger(TestCommand.class);

	private final Map<String, Consumer<@NonNull PrintWriter>> handlers = newHashMap();

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Parameter private @MonotonicNonNull List<String> unnamed;

	@Parameter(names = "--example") private @MonotonicNonNull String example;

	@Parameter(names = "--config") private @MonotonicNonNull String config;

	@Parameter(names = "--type") private @MonotonicNonNull String type = Type.DESTROY.typeName();

	private @MonotonicNonNull ITopic<WorkerMessage<?>> workerTopic;

	private @MonotonicNonNull ITopic<LifecycleMessage> lifecycleTopic;

	public TestCommand() {
		handlers.put(Operation.LIST_EXAMPLES.operationName(), this::listExamples);
		handlers.put(Operation.EXECUTE.operationName(), this::executeExample);
		handlers.put(Operation.COMPUTATION_INTERRUPTED.operationName(), this::computationInterrupted);
	}

	@EnsuresNonNull({"workerTopic", "lifecycleTopic"}) @PostConstruct private void construct() {
		workerTopic = hazelcastInstance.getTopic(DefaultWorkerService.CHANNEL_NAME);
		lifecycleTopic = hazelcastInstance.getTopic(DefaultNodeLifecycleService.CHANNEL_NAME);
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

	private void listExamples(final @NonNull PrintWriter printWriter) {
		log.debug("Listing examples.");
		try {
			final ClassPath classPath = ClassPath.from(TestCommand.class.getClassLoader());
			final ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(EXAMPLES_PACKAGE);
			log.debug("Class path {}.", classes);
			classes.forEach(klass -> printWriter.println(klass.getSimpleName()));
		} catch (final IOException e) {
			log.error("Cannot load classes.", e);
			printWriter.println("Error: Cannot load classes.");
		}
	}

	private void executeExample(final @NonNull PrintWriter printWriter) {
		if (nonNull(config)) {
			runConfig(printWriter);
		} else if (nonNull(example)) {
			runExample();
		}

		try {
			TimeUnit.SECONDS.sleep(1L);
		} catch (final InterruptedException e) {
			log.debug("Interrupted.", e);
		}
		workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));
	}

	private void runExample() {
		log.debug("Executing example.");
		final String className = EXAMPLES_PACKAGE + '.' + example;

		workerTopic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CLASS, className));
	}

	private void runConfig(final @NonNull PrintWriter printWriter) {
		log.debug("Running config.");
		final Path path = Paths.get(config);
		if (!Files.exists(path)) {
			printWriter.println("File " + config + " does not exist.");
			return;
		}
		workerTopic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CONFIGURATION,
		                                                             path.normalize().toString()));
	}

	/**
	 * Operation to test interrupted computation.
	 *
	 * Currently it can run:
	 * # a computation stopped by cluster destruction,
	 * # a computation stopping because of its own error.
	 *
	 * @param printWriter Print writer.
	 */
	private void computationInterrupted(final @NonNull PrintWriter printWriter) {
		log.debug("Testing interrupted computation.");

		printWriter.println("Loading class...");
		final String className = type.equals(Type.COMPUTE_ERROR.typeName())
		                         ? SimpleLongRunningWithError.class.getCanonicalName()
		                         : SimpleLongRunning.class.getCanonicalName();
		workerTopic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CLASS, className));

		printWriter.println("Starting computation...");
		workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));

		printWriter.println("Waiting...");
		try {
			TimeUnit.SECONDS.sleep(10L);
		} catch (final InterruptedException e) {
			log.debug("Interrupted.", e);
		}

		if (type.equals(Type.DESTROY.typeName())) {
			printWriter.println("Destroying cluster...");
			lifecycleTopic.publish(LifecycleMessage.createWithoutPayload(LifecycleMessage.Type.DESTROY));
		}
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
