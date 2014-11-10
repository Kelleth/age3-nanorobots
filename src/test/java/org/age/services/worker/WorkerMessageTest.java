package org.age.services.worker;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

import java.io.Serializable;

import com.google.common.collect.ImmutableSet;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WorkerMessageTest {

	private static final String RECEIVER = "receiver";

	private static final String OTHER_RECEIVER = "other_receiver";

	private static final String PAYLOAD = "payload";

	@Test public void testCreateBroadcastWithoutPayload()  {
		final WorkerMessage<Serializable> message = WorkerMessage.createBroadcastWithoutPayload(
				WorkerMessage.Type.START_COMPUTATION);

		assertThat(message.isBroadcast()).isTrue();
		assertThat(message.recipients()).isEmpty();
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isTrue();
		assertThat(message.payload().isPresent()).isFalse();
	}

	@Test public void testCreateWithoutPayload()  {
		final WorkerMessage<Serializable> message = WorkerMessage.createWithoutPayload(
				WorkerMessage.Type.START_COMPUTATION, newHashSet(RECEIVER));

		assertThat(message.isBroadcast()).isFalse();
		assertThat(message.recipients()).containsOnly(RECEIVER);
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isFalse();
		assertThat(message.payload().isPresent()).isFalse();
	}

	@Test public void testCreateBroadcastWithPayload() {
		final WorkerMessage<Serializable> message = WorkerMessage.createBroadcastWithPayload(
				WorkerMessage.Type.START_COMPUTATION, PAYLOAD);

		assertThat(message.isBroadcast()).isTrue();
		assertThat(message.recipients()).isEmpty();
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isTrue();
		assertThat(message.payload().get()).isEqualTo(PAYLOAD);
	}

	@Test public void testCreateWithPayload()  {
		final WorkerMessage<Serializable> message = WorkerMessage.createWithPayload(
				WorkerMessage.Type.START_COMPUTATION, newHashSet(RECEIVER), PAYLOAD);

		assertThat(message.isBroadcast()).isFalse();
		assertThat(message.recipients()).containsOnly(RECEIVER);
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isFalse();
		assertThat(message.payload().get()).isEqualTo(PAYLOAD);
	}

	@Test public void testBroadcastMessage_correct()  {
		final WorkerMessage message = new WorkerMessage(WorkerMessage.Type.LOAD_CLASS, PAYLOAD);

		assertThat(message.isBroadcast()).isTrue();
		assertThat(message.recipients()).isEmpty();
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isTrue();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testBroadcastMessage_wrongType()  {
		new WorkerMessage<>(WorkerMessage.Type.UNICAST_CONTROL, PAYLOAD);
	}

	@Test public void testUnicastMessage_correct()  {
		final WorkerMessage message = new WorkerMessage(WorkerMessage.Type.LOAD_CLASS, ImmutableSet.of(RECEIVER), PAYLOAD);

		assertThat(message.isBroadcast()).isFalse();
		assertThat(message.recipients()).containsOnly(RECEIVER);
		assertThat(message.isRecipient(RECEIVER)).isTrue();
		assertThat(message.isRecipient(OTHER_RECEIVER)).isFalse();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testUnicastMessage_emptyRecipients()  {
		new WorkerMessage<>(WorkerMessage.Type.LOAD_CLASS, ImmutableSet.of(), PAYLOAD);
	}
}