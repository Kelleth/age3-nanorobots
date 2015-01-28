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
 * Created: 2014-11-04.
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Objects.requireNonNull;

import org.age.annotation.ForTestsOnly;
import org.age.compute.api.UnicastMessageListener;
import org.age.compute.api.UnicastMessenger;
import org.age.compute.api.WorkerAddress;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public final class DefaultUnicastMessenger implements UnicastMessenger, CommunicationFacility {

	private static final Logger log = LoggerFactory.getLogger(DefaultUnicastMessenger.class);

	private final Set<UnicastMessageListener<Serializable>> listeners = newConcurrentHashSet();

	/**
	 * Addresses of workers - collected by the messenger itself.
	 */
	private final Set<WorkerAddress> computeNeighbours = newConcurrentHashSet();

	private final WorkerAddress localWorkerAddress = new DefaultWorkerAddress();

	/**
	 * Topology service - needed for obtaining the node-level neighbours.
	 */
	@Inject @Named("default") private @NonNull TopologyService topologyService;

	@Inject private @NonNull WorkerCommunication workerCommunication;

	@Override @Immutable @NonNull public WorkerAddress address() {
		return localWorkerAddress;
	}

	@Override @NonNull public Set<WorkerAddress> neighbours() {
		return ImmutableSet.copyOf(computeNeighbours);
	}

	@Override
	public <T extends Serializable> void send(final @NonNull WorkerAddress receiver, final @NonNull T message) {
		send(ImmutableSet.of(requireNonNull(receiver)), message);
	}

	@Override
	public <T extends Serializable> void send(final @NonNull Set<WorkerAddress> receivers, final @NonNull T message) {
		final UnicastMessengerMessage unicastMessage = new UnicastMessengerMessage(localWorkerAddress, requireNonNull(receivers),
		                                                         requireNonNull(message));
		log.debug("Sending message {}.", unicastMessage);
		final Set<String> neighbours = topologyService.neighbours();
		final WorkerMessage<Serializable> workerMessage = WorkerMessage.createWithPayload(
				WorkerMessage.Type.UNICAST_MESSAGE, neighbours, unicastMessage);
		log.debug("Prepared message to send: {}.", workerMessage);
		workerCommunication.sendMessage(workerMessage);
	}

	@Override public <T extends Serializable> void registerListener(final @NonNull UnicastMessageListener<T> listener) {
		log.debug("Adding listener {}.", listener);
		listeners.add((UnicastMessageListener<Serializable>)listener);
	}

	@Override public <T extends Serializable> void removeListener(final @NonNull UnicastMessageListener<T> listener) {
		log.debug("Removing listener {}.", listener);
		listeners.remove(listener);
	}

	@Override public <T extends Serializable> boolean onMessage(final @NonNull WorkerMessage<T> workerMessage) {
		log.debug("Received worker message {}.", workerMessage);
		requireNonNull(workerMessage);

		if (workerMessage.hasType(WorkerMessage.Type.UNICAST_MESSAGE)) {
			final UnicastMessengerMessage unicastMessage = (UnicastMessengerMessage)workerMessage.requiredPayload();

			if (unicastMessage.isRecipient(localWorkerAddress)) {
				log.debug("Delivering the message {}.", unicastMessage);
				listeners.parallelStream()
				         .forEach(listener -> listener.onMessage(unicastMessage.payload(), unicastMessage.sender()));
			}

			return true;
		} else if (workerMessage.hasType(WorkerMessage.Type.UNICAST_CONTROL)) {
			final WorkerAddress neighbourWorkerAddress = workerMessage.requiredPayload();
			log.debug("Adding new neighbour: {}.", neighbourWorkerAddress);
			computeNeighbours.add(neighbourWorkerAddress);
			return true;
		}

		return false;
	}

	@Override @NonNull public Set<WorkerMessage.Type> subscribedTypes() {
		return ImmutableSet.of(WorkerMessage.Type.UNICAST_CONTROL, WorkerMessage.Type.UNICAST_MESSAGE);
	}

	@Override public void start() {
		log.debug("Starting local unicast messenger.");
		workerCommunication.scheduleAtFixedRate(this::broadcastMyAddress, 1L, 5L, TimeUnit.SECONDS);
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

	@ForTestsOnly void addNeighbour(final @NonNull WorkerAddress workerAddress) {
		log.debug("Adding new neighbour: {}.", workerAddress);
		computeNeighbours.add(workerAddress);
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(localWorkerAddress).toString();
	}
}


