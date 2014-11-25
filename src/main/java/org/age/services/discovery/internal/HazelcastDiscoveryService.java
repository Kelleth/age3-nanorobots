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
 * Created: 2014-08-21
 */

package org.age.services.discovery.internal;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import org.age.services.discovery.DiscoveryService;
import org.age.services.discovery.MemberAddedEvent;
import org.age.services.discovery.MemberRemovedEvent;
import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.query.SqlPredicate;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public final class HazelcastDiscoveryService implements SmartLifecycle, DiscoveryService {

	private static final String MEMBERS_MAP = "discovery/members";

	private static final Logger log = LoggerFactory.getLogger(HazelcastDiscoveryService.class);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(
			newSingleThreadScheduledExecutor());

	private final AtomicBoolean running = new AtomicBoolean(false);

	@Inject private @MonotonicNonNull HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull NodeIdentityService identityService;

	@Inject private @MonotonicNonNull EventBus eventBus;

	private @MonotonicNonNull IMap<@NonNull String, @NonNull NodeDescriptor> members;

	private @MonotonicNonNull String nodeId;

	private @MonotonicNonNull String entryListenerId;

	@PostConstruct private void construct() {
		nodeId = identityService.nodeId();
		members = hazelcastInstance.getMap(MEMBERS_MAP);
		entryListenerId = members.addEntryListener(new NeighbourMapListener(), true);
	}

	@Override public boolean isAutoStartup() {
		return true;
	}

	@Override public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override public void start() {
		log.debug("Discovery service starting.");
		log.debug("Hazelcast instance: {}.", hazelcastInstance);
		log.debug("Neighbourhood map: {}.", members);
		running.set(true);
		hazelcastInstance.getLifecycleService().addLifecycleListener(this::onHazelcastStateChange);
		log.debug("Waiting for initialization to complete.");
		updateMap();
		final ListenableScheduledFuture<?> mapUpdateTask = executorService.scheduleAtFixedRate(this::updateMap, 10L,
		                                                                                       10L, TimeUnit.SECONDS);
		Futures.addCallback(mapUpdateTask, new FutureCallback<Object>() {
			@Override public void onSuccess(final Object result) {
				// Empty
			}

			@Override public void onFailure(final @NonNull Throwable t) {
				log.error("Map update failed.", t);
			}
		});
		log.info("Discovery service started.");
	}

	@Override public void stop() {
		log.debug("Discovery service stopping.");
		if (hazelcastInstance.getLifecycleService().isRunning()) {
			cleanUp();
		}
		shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);
		running.set(false);
		log.info("Discovery service stopped.");
	}

	@Override public boolean isRunning() {
		return running.get();
	}

	@Override public int getPhase() {
		return Integer.MIN_VALUE + 1;
	}

	@Override public @NonNull @Immutable Set<@NonNull NodeDescriptor> membersMatching(@NonNull final String criteria) {
		return ImmutableSet.copyOf(members.values(new SqlPredicate(requireNonNull(criteria))));
	}

	@Override public @NonNull @Immutable Set<@NonNull NodeDescriptor> allMembers() {
		return ImmutableSet.copyOf(members.values());
	}

	// Wait for "shutting down" event to clean up
	private void onHazelcastStateChange(final LifecycleEvent event) {
		log.debug("Hazelcast lifecycle event: {}.", event);
		if (event.getState() == LifecycleEvent.LifecycleState.SHUTTING_DOWN) {
			cleanUp();
		}
	}

	private void updateMap() {
		log.debug("Updating my info in the members map.");
		members.set(nodeId, identityService.descriptor());
		log.debug("Finished update.");
	}

	private void cleanUp() {
		members.removeEntryListener(entryListenerId);
		log.debug("Deleting myself from the members map.");
		members.delete(nodeId);
	}

	@Immutable
	private class NeighbourMapListener implements EntryListener<@NonNull String, @NonNull NodeDescriptor> {
		@Override public void entryAdded(final EntryEvent<@NonNull String, @NonNull NodeDescriptor> event) {
			log.debug("NeighbourMapListener add event: {}.", event);
			eventBus.post(new MemberAddedEvent(event.getValue()));
		}

		@Override public void entryRemoved(final EntryEvent<@NonNull String, @NonNull NodeDescriptor> event) {
			log.debug("NeighbourMapListener remove event: {}.", event);
			eventBus.post(new MemberRemovedEvent());
		}

		@Override public void entryUpdated(final EntryEvent<@NonNull String, @NonNull NodeDescriptor> event) {
			log.debug("NeighbourMapListener update event: {}.", event);
		}

		@Override public void entryEvicted(final EntryEvent<@NonNull String, @NonNull NodeDescriptor> event) {
			log.debug("NeighbourMapListener evict event: {}.", event);
			eventBus.post(new MemberRemovedEvent());
		}

		@Override public void mapEvicted(final MapEvent event) {
			log.debug("NeighbourMapListener map evict event: {}.", event);
		}

		@Override public void mapCleared(final MapEvent event) {
			log.debug("NeighbourMapListener map clear event: {}.", event);
		}
	}
}
