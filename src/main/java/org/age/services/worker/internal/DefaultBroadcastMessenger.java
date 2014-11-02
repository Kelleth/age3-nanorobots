/*
 * Created: 2014-10-21
 * $Id$
 */

package org.age.services.worker.internal;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.age.compute.api.BroadcastMessenger;
import org.age.compute.api.MessageListener;
import org.age.services.identity.NodeIdentity;
import org.age.services.identity.NodeIdentityService;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerMessage.Type;
import org.age.services.worker.WorkerService;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;

import static com.google.common.collect.Sets.newConcurrentHashSet;

@Named
public class DefaultBroadcastMessenger
		implements BroadcastMessenger, com.hazelcast.core.MessageListener<WorkerMessage> {

	private static final Logger log = LoggerFactory.getLogger(DefaultBroadcastMessenger.class);

	@MonotonicNonNull @Inject private HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull TopologyService topologyService;

	@MonotonicNonNull @Inject private NodeIdentityService identityService;

	@MonotonicNonNull private ITopic<WorkerMessage> topic;

	private final Set<MessageListener<Serializable>> listeners = newConcurrentHashSet();

	@PostConstruct public void construct() {
		log.debug("Constructing BroadcastListener.");

		topic = hazelcastInstance.getTopic(WorkerService.CHANNEL_NAME);
		topic.addMessageListener(this);
	}

	@Override public void send(@NonNull final Serializable message) {
		log.debug("Sending message {}.", message);
		final Set<NodeIdentity> neighbours = topologyService.getNeighbours();
		final Set<String> recipients = neighbours.stream().map(NodeIdentity::getId).collect(Collectors.toSet());
		final WorkerMessage workerMessage = WorkerMessage.createWithPayload(Type.COMPUTATION_MESSAGE, recipients,
		                                                                  message);
		log.debug("Prepared message to send: {}.", workerMessage);
		topic.publish(workerMessage);
	}

	@Override public void onMessage(final Message<WorkerMessage> hazelcastMessage) {
		log.debug("Received Hazelcast message {}.", hazelcastMessage);
		final WorkerMessage object = requireNonNull(hazelcastMessage).getMessageObject();

		if (!object.isRecipient(identityService.getNodeId())) {
			log.debug("Message {} was not directed to me.", object);
			return;
		}

		if (object.hasType(Type.COMPUTATION_MESSAGE)) {
			final Optional<@Nullable Serializable> payload = object.getPayload();
			if (!payload.isPresent()) {
				throw new RuntimeException("Payload required for this hazelcastMessage type.");
			}

			final Serializable message = payload.get();
			listeners.parallelStream().forEach(listener -> listener.onMessage(message));
		}
	}

	@Override public <T extends Serializable> void registerListener(@NonNull final MessageListener<T> listener) {
		log.debug("Adding listener {}.", listener);
		listeners.add((MessageListener<Serializable>)listener);
	}

	@Override public <T extends Serializable> void removeListener(@NonNull final MessageListener<T> listener) {
		log.debug("Removing listener {}.", listener);
		listeners.remove(listener);
	}
}
