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
 * Created: 2014-10-16.
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.nonNull;

import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.lifecycle.LifecycleMessage;
import org.age.services.lifecycle.internal.DefaultNodeLifecycleService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.internal.DefaultWorkerService;
import org.age.services.worker.internal.SingleClassConfiguration;
import org.age.services.worker.internal.SpringConfiguration;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
public final class ComputationCommand extends BaseCommand {

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

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject private @NonNull DiscoveryService discoveryService;

	@Parameter(names = "--class") private String classToLoad;

	@Parameter(names = "--config") private String configToLoad;

	private @MonotonicNonNull ITopic<LifecycleMessage> lifecycleTopic;

	private @MonotonicNonNull ITopic<WorkerMessage<?>> workerTopic;

	private @MonotonicNonNull Map<DefaultWorkerService.ConfigurationKey, Object> workerConfigurationMap;

	public ComputationCommand() {
		addHandler(Operation.LOAD.operationName(), this::load);
		addHandler(Operation.INFO.operationName(), this::info);
		addHandler(Operation.START.operationName(), this::start);
		addHandler(Operation.STOP.operationName(), this::stop);
	}

	@PostConstruct private void construct() {
		lifecycleTopic = hazelcastInstance.getTopic(DefaultNodeLifecycleService.CHANNEL_NAME);
		workerTopic = hazelcastInstance.getTopic(DefaultWorkerService.CHANNEL_NAME);
		workerConfigurationMap = hazelcastInstance.getReplicatedMap(DefaultWorkerService.CONFIGURATION_MAP_NAME);
	}

	@Override public @NonNull Set<String> operations() {
		return Arrays.stream(Operation.values()).map(Operation::operationName).collect(Collectors.toSet());
	}

	private void load(final @NonNull PrintWriter printWriter) {
		if (nonNull(classToLoad)) {
			log.debug("Loading class {}.", classToLoad);

			final SingleClassConfiguration configuration = new SingleClassConfiguration(classToLoad);
			workerConfigurationMap.put(DefaultWorkerService.ConfigurationKey.CONFIGURATION, configuration);
			try {
				TimeUnit.SECONDS.sleep(1L);
			} catch (final InterruptedException e) {
				log.debug("Interrupted.", e);
			}
			workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.LOAD_CONFIGURATION));
		} else if (nonNull(configToLoad)) {
			log.debug("Loading config from {}.", configToLoad);

			try {
				final SpringConfiguration configuration = new SpringConfiguration(configToLoad);
				workerConfigurationMap.put(DefaultWorkerService.ConfigurationKey.CONFIGURATION, configuration);
			} catch (final FileNotFoundException ignored) {
				printWriter.println("File " + configToLoad + " does not exist.");
			}
			try {
				TimeUnit.SECONDS.sleep(1L);
			} catch (final InterruptedException e) {
				log.debug("Interrupted.", e);
			}
			workerTopic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.LOAD_CONFIGURATION));
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
