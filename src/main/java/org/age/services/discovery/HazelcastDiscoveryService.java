/*
 * Created: 2014-08-21
 * $Id$
 */

package org.age.services.discovery;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import org.age.services.identity.NodeIdentity;
import org.age.services.identity.NodeIdentityService;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.MapEvent;
import com.hazelcast.query.SqlPredicate;

import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;

@Named
public class HazelcastDiscoveryService implements SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(HazelcastDiscoveryService.class);
	private final ScheduledExecutorService executorService = newSingleThreadScheduledExecutor();
	private final AtomicBoolean running = new AtomicBoolean(false);
	@Inject @MonotonicNonNull private HazelcastInstance hazelcastInstance;
	@Inject @MonotonicNonNull private NodeIdentityService identityService;
	@Inject @MonotonicNonNull private EventBus eventBus;
	@MonotonicNonNull private ScheduledFuture<?> future;

	@MonotonicNonNull private IMap<@NonNull String, @NonNull NodeIdentity> members;

	@MonotonicNonNull private String nodeId;

	@MonotonicNonNull private String entryListenerId;

	@PostConstruct
	public void construct() {
		nodeId = identityService.getNodeId();
		members = hazelcastInstance.getMap("discovery/members");
		entryListenerId = members.addEntryListener(new NeighbourMapListener(), true);
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	private void cleanUp() {
		members.removeEntryListener(entryListenerId);
		log.debug("Deleting myself from the members map.");
		members.delete(nodeId);
	}

	@Override
	public void start() {
		log.debug("Discovery service starting.");
		log.debug("Hazelcast instance: {}.", hazelcastInstance);
		log.debug("Neighbourhood map: {}.", members);
		running.set(true);
		hazelcastInstance.getLifecycleService().addLifecycleListener(this::hazelcastStateChanged);
		log.debug("Waiting for initialization to complete.");
		updateMap();
		future = executorService.scheduleAtFixedRate(this::updateMap, 10, 10, TimeUnit.SECONDS);
		log.info("Discovery service started.");
	}

	@Override
	public void stop() {
		log.debug("Discovery service stopping.");
		if (hazelcastInstance.getLifecycleService().isRunning()) {
			cleanUp();
		}
		shutdownAndAwaitTermination(executorService, 10, TimeUnit.SECONDS);
		running.set(false);
		log.info("Discovery service stopped.");
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	private void updateMap() {
		log.debug("Updating my info in the members map.");
		members.set(nodeId, identityService.getNodeIdentity());
		log.debug("Finished update.");
	}

	@Override
	public int getPhase() {
		return Integer.MIN_VALUE + 1;
	}

	@NonNull @Immutable
	public Set<@NonNull NodeIdentity> getMembers(@NonNull final String criteria) {
		return ImmutableSet.copyOf(members.values(new SqlPredicate(requireNonNull(criteria))));
	}

	@NonNull @Immutable
	public Set<@NonNull NodeIdentity> getMembers() {
		return ImmutableSet.copyOf(members.values());
	}

	private void hazelcastStateChanged(final LifecycleEvent event) {
		log.debug("Hazelcast lifecycle event: {}.", event);
		if (LifecycleEvent.LifecycleState.SHUTTING_DOWN.equals(event.getState())) {
			cleanUp();
		}
	}

	@Immutable
	private class NeighbourMapListener implements EntryListener<@NonNull String, @NonNull NodeIdentity> {

		@Override
		public void entryAdded(final EntryEvent<@NonNull String, @NonNull NodeIdentity> event) {
			log.debug("NeighbourMapListener add event: {} {}.", event, event.getValue());
			eventBus.post(new DiscoveryEvent());
		}

		@Override
		public void entryRemoved(final EntryEvent<@NonNull String, @NonNull NodeIdentity> event) {
			log.debug("NeighbourMapListener remove event: {}.", event);
			eventBus.post(new DiscoveryEvent());
		}

		@Override
		public void entryUpdated(final EntryEvent<@NonNull String, @NonNull NodeIdentity> event) {
			log.debug("NeighbourMapListener update event: {} {}.", event, event.getValue());
		}

		@Override
		public void entryEvicted(final EntryEvent<@NonNull String, @NonNull NodeIdentity> event) {
			log.debug("NeighbourMapListener evict event: {}.", event);
			eventBus.post(new DiscoveryEvent());
		}

		@Override
		public void mapEvicted(final MapEvent event) {
			log.debug("NeighbourMapListener map evict event: {}.", event);
		}

		@Override
		public void mapCleared(final MapEvent event) {
			log.debug("NeighbourMapListener map clear event: {}.", event);
		}
	}
}
