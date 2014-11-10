package org.age.services.worker.internal;

import java.io.Serializable;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeIdentityService;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerMessage.Type;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.ITopic;

public class DefaultBroadcastMessengerTest {

	private static final String NODE1_ID = "1";

	private static final String NODE2_ID = "2";

	private static final String NODE3_ID = "3";

	private static final Serializable MESSAGE = "message";

	@Mock private TopologyService topologyService;

	@Mock private NodeIdentityService identityService;

	@Mock private ITopic<WorkerMessage> topic;

	@InjectMocks private DefaultBroadcastMessenger messenger;

	@BeforeMethod public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(identityService.getNodeId()).thenReturn(NODE1_ID);
		when(topologyService.getNeighbours()).thenReturn(ImmutableSet.of(NODE2_ID, NODE3_ID));
	}

	@Test public void testSend() {
		final ArgumentCaptor<WorkerMessage> captor = ArgumentCaptor.forClass(WorkerMessage.class);

		messenger.send(MESSAGE);

		verify(topic).publish(captor.capture());
		verifyNoMoreInteractions(topic);
		final WorkerMessage value = captor.getValue();
		assertThat(value.getType(), is(equalTo(Type.COMPUTATION_MESSAGE)));
		assertThat(value.getPayload().get(), is(equalTo(MESSAGE)));
	}
}