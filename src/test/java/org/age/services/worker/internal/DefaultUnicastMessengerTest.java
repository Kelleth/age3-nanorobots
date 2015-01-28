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
 * Created: 2015-01-28.
 */

package org.age.services.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.age.compute.api.UnicastMessageListener;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;

import com.google.common.collect.ImmutableSet;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Set;

public class DefaultUnicastMessengerTest {

	private final String NODE_ADDRESS = "ADDR";

	private final Set<String> NEIGHBOURS = ImmutableSet.of(NODE_ADDRESS);

	private final String PAYLOAD = "payload";

	@Mock private TopologyService topologyService;

	@Mock private WorkerCommunication workerCommunication;

	@Mock private UnicastMessageListener<String> listener;

	@InjectMocks private DefaultUnicastMessenger messenger;

	@BeforeMethod public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(topologyService.neighbours()).thenReturn(NEIGHBOURS);
	}

	@AfterMethod public void tearDown() {

	}

	@Test public void testSend() {
		messenger.send(ImmutableSet.of(messenger.address()), PAYLOAD);

		final ArgumentCaptor<WorkerMessage> argumentCaptor = ArgumentCaptor.forClass(WorkerMessage.class);
		verify(workerCommunication).sendMessage(argumentCaptor.capture());

		final WorkerMessage<Serializable> workerMessage = argumentCaptor.getValue();
		final Serializable payload = workerMessage.payload().get();

		assertThat(workerMessage.hasType(WorkerMessage.Type.UNICAST_MESSAGE)).isTrue();
		assertThat(workerMessage.recipients()).contains(NODE_ADDRESS);
		assertThat(payload).isExactlyInstanceOf(UnicastMessengerMessage.class);

		final UnicastMessengerMessage unicastMessengerMessage = (UnicastMessengerMessage)payload;
		assertThat(unicastMessengerMessage.recipients()).containsExactly(messenger.address());
		assertThat(unicastMessengerMessage.payload()).isEqualTo(PAYLOAD);
	}

	@Test(expectedExceptions = IllegalStateException.class) public void testSend_withoutRecipients() {
		messenger.send(ImmutableSet.of(), PAYLOAD);
	}

	@Test public void testListenersRegistration() {
		messenger.addNeighbour(messenger.address());
		messenger.registerListener(listener);

		final WorkerMessage<UnicastMessengerMessage> message = WorkerMessage.createWithPayload(
				WorkerMessage.Type.UNICAST_MESSAGE, NEIGHBOURS,
				new UnicastMessengerMessage(messenger.address(), ImmutableSet.of(messenger.address()), NODE_ADDRESS));
		messenger.onMessage(message);

		messenger.removeListener(listener);

		messenger.onMessage(message);

		verify(listener, atMost(1)).onMessage(NODE_ADDRESS, messenger.address());
	}

	@Test public void testSubscribedTypes() {
		assertThat(messenger.subscribedTypes()).containsExactly(WorkerMessage.Type.UNICAST_CONTROL,
		                                                        WorkerMessage.Type.UNICAST_MESSAGE);
	}
}