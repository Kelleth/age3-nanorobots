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
 * Created: 2015-02-09.
 */

package org.age.integration;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.age.services.topology.TopologyService;
import org.age.services.worker.TaskStartedEvent;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerServiceEvent;
import org.age.services.worker.internal.ComputationState;
import org.age.services.worker.internal.DefaultWorkerService;
import org.age.services.worker.internal.SpringClasspathConfiguration;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

@ContextConfiguration("classpath:spring-test-node.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public final class MultipleComputationsIT extends AbstractTestNGSpringContextTests {

	@Inject private @NonNull DefaultWorkerService workerService;

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject @Named("default") private @NonNull TopologyService topologyService;

	@Inject private @NonNull EventBus eventBus;

	private @MonotonicNonNull ITopic<WorkerMessage<Serializable>> topic;

	private @MonotonicNonNull Map<DefaultWorkerService.ConfigurationKey, Object> configurationMap;

	private final List<WorkerServiceEvent> events = newCopyOnWriteArrayList();

	@BeforeClass(groups = "integration") public void globalSetUp() {
		topic = hazelcastInstance.getTopic(DefaultWorkerService.CHANNEL_NAME);
		configurationMap = hazelcastInstance.getReplicatedMap(DefaultWorkerService.CONFIGURATION_MAP_NAME);
		eventBus.register(this);
	}

	@BeforeMethod(groups = "integration") public void setUp() {
		MockitoAnnotations.initMocks(this);

	}

	@Test(groups = "integration") public void testMultipleComputations() throws FileNotFoundException, InterruptedException {
		for (int i = 0; i < 2; i++) {

			final SpringClasspathConfiguration configuration = new SpringClasspathConfiguration(
					"org/age/example/spring-simple.xml");
			configurationMap.put(DefaultWorkerService.ConfigurationKey.CONFIGURATION, configuration);

			topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.LOAD_CONFIGURATION));

			TimeUnit.SECONDS.sleep(3L);

			assertThat(computationState()).isEqualTo(ComputationState.CONFIGURED);

			topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));

			TimeUnit.SECONDS.sleep(3L);

			assertThat(computationState()).isEqualTo(ComputationState.FINISHED);

			assertThat(events).hasAtLeastOneElementOfType(TaskStartedEvent.class);

			topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.CLEAN_CONFIGURATION));

			TimeUnit.SECONDS.sleep(3L);

			assertThat(computationState()).isEqualTo(ComputationState.NONE);
		}
	}

	private <T> @NonNull Optional<T> configurationValue(final DefaultWorkerService.ConfigurationKey key,
	                                                    final @NonNull Class<T> klass) {
		return Optional.ofNullable((T)configurationMap.get(key));
	}

	private @NonNull ComputationState computationState() {
		return configurationValue(DefaultWorkerService.ConfigurationKey.COMPUTATION_STATE,
		                          ComputationState.class).orElseGet(() -> ComputationState.NONE);
	}

	@Subscribe public void listenForEvents(final @NonNull WorkerServiceEvent event) {
		events.add(event);
	}
}
