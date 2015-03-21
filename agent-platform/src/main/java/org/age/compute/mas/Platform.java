/*
 * Copyright (C) 2014-2015 Intelligent Information Systems Group.
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

package org.age.compute.mas;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static java.util.Objects.requireNonNull;
import static org.age.compute.mas.util.TimeMeasurement.measureTime;

import org.age.compute.mas.agent.Agent;
import org.age.compute.mas.agent.internal.AgentBuilder;
import org.age.compute.mas.agent.Workplace;
import org.age.compute.mas.agent.internal.InternalAgentView;
import org.age.compute.mas.configuration.AgentDescriptor;
import org.age.compute.mas.configuration.Configuration;
import org.age.compute.mas.configuration.WorkplaceDescriptor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main class for running agents platform
 */
public final class Platform implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Platform.class);

	private final Configuration configuration;

	private final List<Workplace> workplaces;

	public Platform(final Configuration configuration) {
		this.configuration = requireNonNull(configuration);
		workplaces = measureTime(() -> instantiateWorkplaces(configuration.workplaces()), "Workplaces created in: ");
	}

	private List<Workplace> instantiateWorkplaces(final List<WorkplaceDescriptor> workplaces) {
		log.debug("Instantiating {} workplaces.", workplaces.size());
		final ImmutableList.Builder<Workplace> builder = ImmutableList.builder();
		for (int i = 0; i < workplaces.size(); i++) {
			final WorkplaceDescriptor desc = workplaces.get(i);
			final Workplace workplace = new Workplace("workplace-" + i, desc.actions());
			workplace.addChildren(instantiateAgents(workplace, desc.agents()));
			builder.add(workplace);
		}
		log.debug("Finished workplaces instantiation.");
		return builder.build();
	}

	@Override public void run() {
		log.info("MAS starting.");
		final List<Thread> threads = createThreadsForWorkplaces();
		waitUntilStopConditionReached();
		try {
			stopWorkplaces(threads);
		} catch (final InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
		log.info("MAS stopped.");
	}

	private List<Thread> createThreadsForWorkplaces() {
		final List<Thread> threads = newArrayListWithExpectedSize(workplaces.size());
		for (int workplaceIndex = 0; workplaceIndex < workplaces.size(); workplaceIndex++) {
			createWorkplaceThread(threads, workplaceIndex);
		}
		return threads;
	}

	private void createWorkplaceThread(final List<Thread> threads, final int workplaceIndex) {
		final Workplace workplace = workplaces.get(workplaceIndex);
		final Thread thread = new Thread(workplace);
		thread.setName("Workplace-" + workplaceIndex);
		thread.start();
		threads.add(thread);
	}

	private void waitUntilStopConditionReached() {
		while (!configuration.stopCondition().isReached()) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (final InterruptedException e) {
				log.warn("Interrupted", e);
			}
		}
	}

	private void stopWorkplaces(final List<Thread> threads) throws InterruptedException {
		log.debug("Workplaces stop was requested.");
		for (final Thread thread : threads) {
			thread.interrupt();
			thread.join(10_000); // wait up to 10 seconds for thread
		}
		log.info("Workplaces stopped.");
	}

	private List<Agent<?>> instantiateAgents(final Workplace workplace, final List<AgentDescriptor> agentsDescriptors) {
		// here we are creating agents using provided descriptions
		return agentsDescriptors.stream().map(desc -> buildAgent(desc, workplace)).collect(Collectors.toList());
	}

	private Agent<?> buildAgent(final AgentDescriptor descriptor, final Agent<?> parent) {
		final Agent<?> agent = AgentBuilder.create(descriptor.agentClass())
		                                   .withActions(descriptor.actions())
		                                   .withSettings(descriptor.settings())
		                                   .withParent(parent)
		                                   .withName(descriptor.name())
		                                   .build();

		final InternalAgentView internalAgentView = (InternalAgentView)agent;
		descriptor.children()
		          .stream()
		          .map(childDescriptor -> buildAgent(childDescriptor, agent))
		          .forEach(internalAgentView::addChild);

		return agent;
	}

	@VisibleForTesting List<Workplace> workplaces() {
		return ImmutableList.copyOf(workplaces);
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
