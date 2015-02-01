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
 * Created: 2015-01-31.
 */

package org.age.integration;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.age.services.discovery.DiscoveryEvent;
import org.age.services.discovery.MemberAddedEvent;
import org.age.services.discovery.internal.HazelcastDiscoveryService;
import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;
import org.age.services.identity.NodeType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@ContextConfiguration("classpath:spring-test-node.xml")
public class DiscoveryIT extends AbstractTestNGSpringContextTests {

	@Inject private @NonNull HazelcastDiscoveryService discoveryService;

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject private @NonNull NodeIdentityService identityService;

	@Inject private @NonNull EventBus eventBus;

	private @MonotonicNonNull IMap<String, NodeDescriptor> members;

	private @Mock(serializable = true) NodeDescriptor nodeDescriptor;

	private final List<DiscoveryEvent> events = newCopyOnWriteArrayList();

	@BeforeClass(groups = "integration") public void globalSetUp() {
		members = hazelcastInstance.getMap(HazelcastDiscoveryService.MEMBERS_MAP);
		eventBus.register(this);
	}

	@BeforeMethod(groups = "integration") public void setUp() {
		MockitoAnnotations.initMocks(this);
		events.clear();
	}

	@Test(groups = "integration", description = "Is service running?") public void testIfIsRunning() {
		assertThat(discoveryService.isRunning()).isTrue();
	}

	@Test(groups = "integration", description = "Is service putting the local node descriptor into the map?")
	public void testIfServicePutsItselfIntoMap() throws InterruptedException {
		final String nodeId = identityService.nodeId();
		// Give it some time to put entry into map
		TimeUnit.SECONDS.sleep(10L);

		assertThat(members).containsOnlyKeys(nodeId);
	}

	@Test(groups = "integration", dependsOnMethods = "testIfServicePutsItselfIntoMap")
	public void testIfServiceCreatesEvents() throws InterruptedException {
		final String nodeId = "test-id";
		when(nodeDescriptor.type()).thenReturn(NodeType.COMPUTE);
		when(nodeDescriptor.id()).thenReturn(nodeId);

		members.put(nodeId, nodeDescriptor);

		TimeUnit.SECONDS.sleep(1L);

		final MemberAddedEvent memberAddedEvent = new MemberAddedEvent(nodeId, NodeType.COMPUTE);
		assertThat(events).usingElementComparatorIgnoringFields("timestamp").contains(memberAddedEvent);
	}

	@Subscribe public void listenForEvents(final @NonNull DiscoveryEvent event) {
		events.add(event);
	}
}
