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
 * Created: 2014-10-21.
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Objects.requireNonNull;

import org.age.compute.api.BroadcastMessenger;
import org.age.compute.api.MessageListener;
import org.age.services.topology.TopologyService;
import org.age.services.worker.WorkerMessage;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@ThreadSafe
public final class DefaultBroadcastMessenger implements BroadcastMessenger, CommunicationFacility {

	private static final Logger log = LoggerFactory.getLogger(DefaultBroadcastMessenger.class);

	private final Set<MessageListener<Serializable>> listeners = newConcurrentHashSet();

	@Inject @Named("default") @MonotonicNonNull private TopologyService topologyService;

	@Inject @MonotonicNonNull private WorkerCommunication workerCommunication;

	@Override public void send(@NonNull final Serializable message) {
		log.debug("Sending message {}.", message);
		final Set<String> neighbours = topologyService.neighbours();
		final WorkerMessage<Serializable> workerMessage = WorkerMessage.createWithPayload(
				WorkerMessage.Type.BROADCAST_MESSAGE, neighbours, message);
		log.debug("Prepared message to send: {}.", workerMessage);
		workerCommunication.sendMessage(workerMessage);
	}

	@Override public <T extends Serializable> boolean onMessage(@NonNull final WorkerMessage<T> workerMessage) {
		log.debug("Received worker service message {}.", workerMessage);
		requireNonNull(workerMessage);

		if (!workerMessage.hasType(WorkerMessage.Type.BROADCAST_MESSAGE)) {
			final Serializable message = workerMessage.requiredPayload();
			listeners.parallelStream().forEach(listener -> listener.onMessage(message));

			return true;
		}

		return false;
	}

	@Override @NonNull public Set<WorkerMessage.Type> subscribedTypes() {
		return ImmutableSet.of(WorkerMessage.Type.BROADCAST_MESSAGE);
	}

	@Override public void start() {
		log.debug("Starting local broadcast messenger.");
	}

	@Override public <T extends Serializable> void registerListener(@NonNull final MessageListener<T> listener) {
		log.debug("Adding listener {}.", listener);
		listeners.add((MessageListener<Serializable>)listener);
	}

	@Override public <T extends Serializable> void removeListener(@NonNull final MessageListener<T> listener) {
		log.debug("Removing listener {}.", listener);
		listeners.remove(listener);
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
