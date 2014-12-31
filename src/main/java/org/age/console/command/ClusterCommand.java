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

import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.lifecycle.LifecycleMessage;
import org.age.services.lifecycle.internal.DefaultNodeLifecycleService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting info about and managing the whole cluster.
 */
@Named
@Scope("prototype")
@Parameters(commandNames = "cluster", commandDescription = "Cluster management", optionPrefixes = "--")
public class ClusterCommand implements Command {

	private enum Operation {
		NODES("nodes"),
		DESTROY("destroy");

		private final String operationName;

		Operation(final @NonNull String operationName) {
			this.operationName = operationName;
		}

		public String operationName() {
			return operationName;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(ClusterCommand.class);

	private final Map<String, Consumer<@NonNull PrintWriter>> handlers = newHashMap();

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject private @NonNull DiscoveryService discoveryService;

	@Parameter private @MonotonicNonNull List<String> unnamed;

	private @MonotonicNonNull ITopic<LifecycleMessage> topic;

	public ClusterCommand() {
		handlers.put(Operation.NODES.operationName(), this::nodes);
		handlers.put(Operation.DESTROY.operationName(), this::destroy);
	}

	@EnsuresNonNull("topic") @PostConstruct private void construct() {
		topic = hazelcastInstance.getTopic(DefaultNodeLifecycleService.CHANNEL_NAME);
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

	private void nodes(final PrintWriter printWriter) {
		log.debug("Printing information about nodes.");
		final Set<NodeDescriptor> neighbours = discoveryService.allMembers();
		neighbours.forEach(printWriter::println);
	}

	private void destroy(final PrintWriter printWriter) {
		log.debug("Destroying cluster.");
		topic.publish(LifecycleMessage.createWithoutPayload(LifecycleMessage.Type.DESTROY));
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
