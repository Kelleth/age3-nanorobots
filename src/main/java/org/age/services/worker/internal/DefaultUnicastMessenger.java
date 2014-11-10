/*
 * Created: 2014-11-04
 * $Id$
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Objects.requireNonNull;

import org.age.compute.api.MessageListener;
import org.age.compute.api.UnicastMessenger;
import org.age.compute.api.WorkerAddress;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public final class DefaultUnicastMessenger implements UnicastMessenger, CommunicationFacility {

	private static final Logger log = LoggerFactory.getLogger(DefaultUnicastMessenger.class);

	private final Set<MessageListener<Serializable>> listeners = newConcurrentHashSet();

	private final Set<WorkerAddress> computeNeighbours = newConcurrentHashSet();

	private final WorkerAddress localWorkerAddress = new DefaultWorkerAddress();

	@Inject @Named("default") @MonotonicNonNull private TopologyService topologyService;

	@Inject @MonotonicNonNull private WorkerCommunication workerCommunication;

	@PostConstruct private void construct() {
		log.debug("Initializing local unicast messenger.");
		workerCommunication.scheduleAtFixedRate(this::broadcastMyAddress, 1L, 5L, TimeUnit.SECONDS);
	}

	@Override @Immutable @NonNull  public WorkerAddress address() {
		return localWorkerAddress;
	}

	@Override @NonNull public Set<WorkerAddress> neighbours() {
		return ImmutableSet.copyOf(computeNeighbours);
	}

	@Override public <T extends Serializable> void send(@NonNull final WorkerAddress receiver,
	                                                    @NonNull final T message) {
		send(ImmutableSet.of(requireNonNull(receiver)), message);
	}

	@Override public <T extends Serializable> void send(@NonNull final Set<WorkerAddress> receivers,
	                                                    @NonNull final T message) {
		checkState(isInitialized(), "Messenger was not initialized.");

		final UnicastMessage unicastMessage = new UnicastMessage(requireNonNull(receivers), requireNonNull(message));
		log.debug("Sending message {}.", unicastMessage);
		final Set<String> neighbours = topologyService.neighbours();
		final WorkerMessage<Serializable> workerMessage = WorkerMessage.createWithPayload(
				WorkerMessage.Type.UNICAST_MESSAGE, neighbours, unicastMessage);
		log.debug("Prepared message to send: {}.", workerMessage);
		workerCommunication.sendMessage(workerMessage);
	}

	@Override public <T extends Serializable> void registerListener(@NonNull final MessageListener<T> listener) {
		log.debug("Adding listener {}.", listener);
		listeners.add((MessageListener<Serializable>)listener);
	}

	@Override public <T extends Serializable> void removeListener(@NonNull final MessageListener<T> listener) {
		log.debug("Removing listener {}.", listener);
		listeners.remove(listener);
	}

	@Override public <T extends Serializable> boolean onMessage(@NonNull final WorkerMessage<T> workerMessage) {
		log.debug("Received worker message {}.", workerMessage);
		requireNonNull(workerMessage);

		if (!isInitialized()) {
			return false;
		}

		if (workerMessage.hasType(WorkerMessage.Type.UNICAST_MESSAGE)) {
			final UnicastMessage unicastMessage = (UnicastMessage)workerMessage.requiredPayload();

			if (unicastMessage.isRecipient(localWorkerAddress)) {
				log.debug("Delivering the message {}.", unicastMessage);
				listeners.parallelStream().forEach(listener -> listener.onMessage(unicastMessage.payload()));
			}
		} else if (workerMessage.hasType(WorkerMessage.Type.UNICAST_CONTROL)) {
			final WorkerAddress neighbourWorkerAddress = workerMessage.requiredPayload();
			log.debug("Adding new neighbour: {}.", neighbourWorkerAddress);
			computeNeighbours.add(neighbourWorkerAddress);
		}

		return false;
	}

	@Override @NonNull public Set<WorkerMessage.Type> subscribedTypes() {
		return ImmutableSet.of(WorkerMessage.Type.UNICAST_CONTROL, WorkerMessage.Type.UNICAST_MESSAGE);
	}

	private void broadcastMyAddress() {
		log.debug("Broadcasting my unicast address: {}.", localWorkerAddress);
		try {
			final Set<String> neighbours = topologyService.neighbours();
			if (neighbours.isEmpty()) {
				log.debug("No neighbours.");
				return;
			}
			final WorkerMessage<Serializable> workerMessage = WorkerMessage.createWithPayload(
					WorkerMessage.Type.UNICAST_CONTROL, neighbours, localWorkerAddress);
			workerCommunication.sendMessage(workerMessage);
		} catch (final IllegalStateException e) {
			log.debug("Topology is not available yet.");
		}
	}

	private boolean isInitialized() {
		return localWorkerAddress != null;
	}

	private static final class UnicastMessage implements Serializable {

		private static final long serialVersionUID = 8710738856544239311L;

		@NonNull private final Set<WorkerAddress> recipients;

		@NonNull private final Serializable payload;

		UnicastMessage(final Set<WorkerAddress> recipients, final Serializable payload) {
			this.recipients = ImmutableSet.copyOf(requireNonNull(recipients));
			this.payload = requireNonNull(payload);
		}

		@NonNull public Set<WorkerAddress> recipients() {
			return recipients;
		}

		public boolean isRecipient(@NonNull final WorkerAddress workerAddress) {
			assert workerAddress != null;
			return recipients.contains(workerAddress);
		}

		@NonNull public Serializable payload() {
			return payload;
		}

		@Override public String toString() {
			return toStringHelper(this).add("recipients", recipients).addValue(payload).toString();
		}
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(localWorkerAddress).toString();
	}
}


