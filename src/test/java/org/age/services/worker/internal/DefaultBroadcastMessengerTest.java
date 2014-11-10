package org.age.services.worker.internal;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerMessage.Type;

import java.io.Serializable;

import com.google.common.collect.ImmutableSet;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public final class DefaultBroadcastMessengerTest {

	private static final String NODE1_ID = "1";

	private static final String NODE2_ID = "2";

	private static final String NODE3_ID = "3";

	private static final Serializable MESSAGE = "message";

	@Mock private TopologyService topologyService;

	@Mock private WorkerCommunication workerCommunication;

	@InjectMocks private DefaultBroadcastMessenger messenger;

	@BeforeMethod public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(topologyService.neighbours()).thenReturn(ImmutableSet.of(NODE2_ID, NODE3_ID));
	}

	@Test public void testSend() {
		final ArgumentCaptor<WorkerMessage> captor = ArgumentCaptor.forClass(WorkerMessage.class);

		messenger.send(MESSAGE);

		verify(workerCommunication).sendMessage(captor.capture());
		verifyNoMoreInteractions(workerCommunication);
		final WorkerMessage value = captor.getValue();
		assertThat(value.type()).isEqualTo(Type.BROADCAST_MESSAGE);
		assertThat(value.payload().get()).isEqualTo(MESSAGE);
	}
}
