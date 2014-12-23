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
 * Created: 2014-08-25
 */

package org.age.services.worker.internal;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newEnumMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import org.age.services.identity.NodeIdentityService;
import org.age.services.lifecycle.NodeDestroyedEvent;
import org.age.services.lifecycle.NodeLifecycleService;
import org.age.services.topology.TopologyService;
import org.age.services.worker.TaskStartedEvent;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerService;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

public class DefaultWorkerService implements SmartLifecycle, WorkerCommunication, WorkerService {

	public static final String CHANNEL_NAME = "worker/channel";

	private static final Logger log = LoggerFactory.getLogger(DefaultWorkerService.class);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(newScheduledThreadPool(5));

	private final Map<WorkerMessage.Type, Set<CommunicationFacility>> workerMessageListeners = newEnumMap(
			WorkerMessage.Type.class);

	private final Set<CommunicationFacility> communicationFacilities = newHashSet();

	private final EnumMap<WorkerMessage.Type, Consumer<Serializable>> messageHandlers = newEnumMap(
			WorkerMessage.Type.class);

	@Inject private @MonotonicNonNull HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull NodeIdentityService identityService;

	@Inject private @MonotonicNonNull NodeLifecycleService lifecycleService;

	@Inject @Named("default") private @MonotonicNonNull TopologyService topologyService;

	@Inject private @MonotonicNonNull EventBus eventBus;

	@Inject private @MonotonicNonNull ApplicationContext applicationContext;

	private @MonotonicNonNull ITopic<WorkerMessage<Serializable>> topic;

	private @Nullable TaskBuilder taskBuilder;

	private @Nullable StartedTask currentTask;

	protected DefaultWorkerService() {
		Arrays.stream(WorkerMessage.Type.values()).forEach(type -> workerMessageListeners.put(type, newHashSet()));

		messageHandlers.put(WorkerMessage.Type.LOAD_CLASS, this::handleLoadClass);
		messageHandlers.put(WorkerMessage.Type.LOAD_CONFIGURATION, this::handleLoadConfig);
		messageHandlers.put(WorkerMessage.Type.START_COMPUTATION, this::handleStartComputation);
		messageHandlers.put(WorkerMessage.Type.STOP_COMPUTATION, this::handleStopComputation);
		messageHandlers.put(WorkerMessage.Type.CLEAN_CONFIGURATION, this::handleCleanConfiguration);
	}

	@PostConstruct private void construct() {
		topic = hazelcastInstance.getTopic(CHANNEL_NAME);
		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);
	}

	@Override public final boolean isAutoStartup() {
		return true;
	}

	@Override public final void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override public final void start() {
		log.debug("Worker service starting.");

		running.set(true);

		log.info("Worker service started.");
	}

	@Override public final void stop() {
		log.debug("Worker service stopping.");

		running.set(false);
		shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);

		log.info("Worker service stopped.");
	}

	@Override public final boolean isRunning() {
		return running.get();
	}

	@Override public final int getPhase() {
		return Integer.MAX_VALUE;
	}

	@Override public void sendMessage(@NonNull final WorkerMessage<Serializable> message) {
		log.debug("Sending message {}.", message);
		topic.publish(message);
	}

	@Override public ListenableScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay,
	                                                                  final long period, final TimeUnit unit) {
		return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	private void handleLoadClass(final @NonNull Serializable payload) {
		assert nonNull(payload) && (payload instanceof String);
		setupTaskFromClass((String)payload);
	}

	private void handleLoadConfig(final @NonNull Serializable payload) {
		assert nonNull(payload) && (payload instanceof String);
		setupTaskFromConfig((String)payload);
	}

	private void handleStartComputation(final @Nullable Serializable payload) {
		assert isNull(payload);
		startTask();
	}

	private void handleStopComputation(final @Nullable Serializable payload) {
		assert isNull(payload);
		stopTask();
	}

	private void handleCleanConfiguration(final @Nullable Serializable payload) {
		assert isNull(payload);
		cleanUpAfterTask();
	}

	private boolean isTaskPresent() {
		return nonNull(taskBuilder) && nonNull(currentTask);
	}

	private boolean isEnvironmentReady() {
		return lifecycleService.isRunning() && topologyService.hasTopology();
	}

	private void setupTaskFromClass(final @NonNull String className) {
		assert nonNull(className);
		checkState(!isTaskPresent(), "Task is already configured.");

		final TaskBuilder classTaskBuilder = TaskBuilder.fromClass(className);
		prepareContext(classTaskBuilder);
		taskBuilder = classTaskBuilder;
	}

	private void setupTaskFromConfig(final @NonNull String configPath) {
		assert nonNull(configPath);
		checkState(!isTaskPresent(), "Task is already configured.");

		final TaskBuilder configTaskBuilder = TaskBuilder.fromConfig(configPath);
		prepareContext(configTaskBuilder);
		taskBuilder = configTaskBuilder;
	}

	private void prepareContext(final @NonNull TaskBuilder taskBuilder) {
		assert nonNull(taskBuilder);

		// Configure communication facilities (as singletons)
		final Map<String, CommunicationFacility> facilitiesMap = applicationContext.getBeansOfType(
				CommunicationFacility.class);
		communicationFacilities.addAll(facilitiesMap.values());
		// Add services injected by the container
		log.debug("Registering facilities and adding them as listeners for messages.");
		communicationFacilities.forEach(service -> {
			service.subscribedTypes().forEach(key -> workerMessageListeners.get(key).add(service));
			log.debug("Registering {} as {} in application context.", service.getClass().getSimpleName(), service);
			taskBuilder.registerSingleton(service);
		});

		// Refreshing the Spring context
		taskBuilder.finishConfiguration();
	}

	private void startTask() {
		assert nonNull(taskBuilder);

		if (!isEnvironmentReady()) {
			log.warn("Trying to start computation when node is not ready.");
			// XXX: Maybe enqueue for future start?
			return;
		}

		log.debug("Starting task {}.", taskBuilder);

		communicationFacilities.forEach(CommunicationFacility::start);
		currentTask = taskBuilder.buildAndSchedule(executorService, new ExecutionListener());
		eventBus.post(new TaskStartedEvent());
	}

	private void stopTask() {
		log.debug("Stopping current task {}.", currentTask);

		if (!isTaskPresent()) {
			log.info("No task to stop.");
			return;
		}
		if (!currentTask.isRunning()) {
			log.warn("Task is already stopped.");
			return;
		}

		currentTask.stop();
	}

	private void cleanUpAfterTask() {
		log.debug("Cleaning up after task {}.", currentTask);

		if (!isTaskPresent()) {
			log.warn("No task to clean up after.");
		}

		currentTask.cleanUp();
		currentTask = null;
		log.debug("Clean up finished.");
	}

	@Subscribe public void handleNodeDestroyedEvent(final @NonNull NodeDestroyedEvent event) {
		log.debug("Got event: {}.", event);
		stopTask();
	}

	private final class DistributedMessageListener implements MessageListener<WorkerMessage<Serializable>> {

		@Override public void onMessage(final Message<WorkerMessage<Serializable>> message) {
			final WorkerMessage<Serializable> workerMessage = requireNonNull(message.getMessageObject());
			log.debug("WorkerMessage received: {}.", workerMessage);

			try {
				if (!workerMessage.isRecipient(identityService.nodeId())) {
					log.debug("Message {} was not directed to me.", workerMessage);
					return;
				}

				final WorkerMessage.Type type = workerMessage.type();
				final Set<CommunicationFacility> listeners = workerMessageListeners.get(type);
				boolean eaten = false;
				for (final CommunicationFacility listener : listeners) {
					log.debug("Notifying listener {}.", listener);
					if (listener.onMessage(workerMessage)) {
						eaten = true;
						break;
					}
				}

				if (eaten) {
					return;
				}

				messageHandlers.get(workerMessage.type()).accept(workerMessage.payload().orElse(null));
			} catch (final Throwable t) {
				log.info("T", t);
			}
		}
	}

	private final class ExecutionListener implements FutureCallback<Object> {

		@Override public void onSuccess(final Object result) {
			log.info("Task {} finished.", currentTask);
			cleanUpAfterTask();
		}

		@Override public void onFailure(final @NonNull Throwable t) {
			log.error("Task {} failed with error.", currentTask, t);
			cleanUpAfterTask();
		}
	}

}
