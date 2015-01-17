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
 * Created: 04.01.2015.
 */

package org.age.services.status.internal;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import org.age.services.ServiceFailureEvent;
import org.age.services.identity.NodeIdentityService;
import org.age.services.lifecycle.NodeLifecycleService;
import org.age.services.status.Status;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.units.qual.s;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public final class DefaultStatusService implements SmartLifecycle {

	public static final String CHANNEL_NAME = "status/channel";

	public static final String MAP_NAME = "status/map";

	private static final @s long UPDATE_PERIOD_IN_S = 1L;

	private static final Logger log = LoggerFactory.getLogger(DefaultStatusService.class);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(
			newSingleThreadScheduledExecutor());

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final List<Throwable> collectedErrors = newArrayListWithCapacity(10);

	@Inject private @NonNull HazelcastInstance hazelcastInstance;

	@Inject private @NonNull NodeIdentityService identityService;

	@Inject private @NonNull NodeLifecycleService lifecycleService;

	@Inject private @NonNull EventBus eventBus;

	private @MonotonicNonNull String nodeId;

	private @MonotonicNonNull IMap<@NonNull String, @NonNull Status> statusMap;

	@EnsuresNonNull({"nodeId", "statusMap"})
	@PostConstruct private void construct() {
		nodeId = identityService.nodeId();
		statusMap = hazelcastInstance.getMap(MAP_NAME);
		eventBus.register(this);
	}

	@Override public boolean isAutoStartup() {
		return true;
	}

	@Override public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override public void start() {
		log.debug("Status service starting.");

		running.set(true);
		final ListenableScheduledFuture<?> mapUpdateTask = executorService.scheduleAtFixedRate(this::updateMap,
		                                                                                       UPDATE_PERIOD_IN_S,
		                                                                                       UPDATE_PERIOD_IN_S,
		                                                                                       TimeUnit.SECONDS);
		Futures.addCallback(mapUpdateTask, new MapUpdateCallback());

		log.info("Status service started.");
	}



	@Override public void stop() {
		log.debug("Status service stopping.");

		running.set(false);
		shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);

		log.info("Status service stopped.");
	}

	@Override public boolean isRunning() {
		return running.get();
	}

	@Override public int getPhase() {
		return 0;
	}

	private void updateMap() {
		final DefaultStatus.Builder statusBuilder = DefaultStatus.Builder.create();
		statusBuilder.addErrors(collectedErrors);
		statusMap.put(nodeId, statusBuilder.buildStatus());
	}

	// Event handlers

	@Subscribe public void handleServiceFailureEvent(final @NonNull ServiceFailureEvent event) {
		log.debug("Service failure event: {}.", event);
		collectedErrors.add(event.cause());
	}

	@Immutable
	private static final class MapUpdateCallback implements FutureCallback<Object> {
		@Override public void onSuccess(final Object result) {
			// Empty
		}

		@Override public void onFailure(final @NonNull Throwable t) {
			log.error("Map update failed.", t);
		}
	}
}
