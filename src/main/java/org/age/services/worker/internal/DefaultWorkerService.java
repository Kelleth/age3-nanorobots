/*
 * Created: 2014-08-25
 */

package org.age.services.worker.internal;

import static com.google.common.collect.Maps.newEnumMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import org.age.services.identity.NodeIdentityService;
import org.age.services.worker.WorkerMessage;
import org.age.services.worker.WorkerService;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class DefaultWorkerService implements SmartLifecycle, WorkerCommunication, WorkerService {

	public static final String CHANNEL_NAME = "worker/channel";

	private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(newScheduledThreadPool(5));

	private final Map<WorkerMessage.Type, Set<CommunicationFacility>> workerMessageListeners = newEnumMap(
			WorkerMessage.Type.class);

	private final Set<CommunicationFacility> communicationFacilities = newHashSet();

	private final ReadWriteLock taskLock = new ReentrantReadWriteLock();

	private final EnumMap<WorkerMessage.Type, Consumer<Serializable>> messageHandlers = newEnumMap(
			WorkerMessage.Type.class);

	@Inject private @MonotonicNonNull HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull NodeIdentityService identityService;

	@Inject private @MonotonicNonNull EventBus eventBus;

	@Inject private @MonotonicNonNull ApplicationContext applicationContext;

	private @MonotonicNonNull ITopic<WorkerMessage<Serializable>> topic;

	@GuardedBy("taskLock") private @Nullable String currentClassName;

	@GuardedBy("taskLock") private @Nullable Runnable currentTask;

	@GuardedBy("taskLock") private @Nullable AbstractApplicationContext currentContext;

	@GuardedBy("taskLock") private @Nullable ListenableScheduledFuture<?> currentTaskFuture;

	protected DefaultWorkerService() {
		Arrays.stream(WorkerMessage.Type.values()).forEach(type -> workerMessageListeners.put(type, newHashSet()));

		messageHandlers.put(WorkerMessage.Type.LOAD_CLASS, this::handleLoadClass);
		messageHandlers.put(WorkerMessage.Type.LOAD_CONFIGURATION, this::handleLoadConfig);
		messageHandlers.put(WorkerMessage.Type.START_COMPUTATION, this::handleStartComputation);
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

	public boolean isSetUp() {
		return currentClassName != null;
	}

	public boolean isTaskRunning() {
		return (currentTaskFuture != null) && !currentTaskFuture.isDone();
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

	private void setupTaskFromClass(final @NonNull String className) {
		assert nonNull(className);
		assert !isTaskRunning() : "Task is already running.";

		taskLock.writeLock().lock();
		try {
			log.debug("Setting up task from class {}.", className);

			log.debug("Creating internal Spring context.");
			final AnnotationConfigApplicationContext taskContext = new AnnotationConfigApplicationContext();

			// Configure task
			final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(className);
			taskContext.registerBeanDefinition("runnable", builder.getBeanDefinition());

			prepareContext(taskContext);
			currentClassName = className;

			log.debug("Task setup finished.");
		} catch (final BeanCreationException e) {
			log.error("Cannot create the task.", e);
			cleanUpAfterTask();
		} finally {
			taskLock.writeLock().unlock();
		}
	}

	private void setupTaskFromConfig(final @NonNull String configPath) {
		assert nonNull(configPath);
		assert !isTaskRunning() : "Task is already running.";

		taskLock.writeLock().lock();
		try {
			log.debug("Setting up task from config {}.", configPath);

			log.debug("Creating internal Spring context.");
			final FileSystemXmlApplicationContext taskContext = new FileSystemXmlApplicationContext(configPath);
			prepareContext(taskContext);

			currentClassName = taskContext.getType("runnable").getCanonicalName();

			log.debug("Task setup finished.");
		} catch (final BeanCreationException e) {
			log.error("Cannot create the task.", e);
			cleanUpAfterTask();
		} finally {
			taskLock.writeLock().unlock();
		}
	}

	private void prepareContext(final @NonNull AbstractApplicationContext taskContext) {
		assert nonNull(taskContext);

		// Configure communication facilities (as singletons)
		final ConfigurableListableBeanFactory beanFactory = taskContext.getBeanFactory();
		final Map<String, CommunicationFacility> facilitiesMap = applicationContext.getBeansOfType(
				CommunicationFacility.class);
		communicationFacilities.addAll(facilitiesMap.values());
		// Add services
		log.debug("Registering facilities and adding them as listeners for messages.");
		communicationFacilities.forEach(service -> {
			service.subscribedTypes().forEach(key -> workerMessageListeners.get(key).add(service));
			log.debug("Registering {} as {} in application context.", service.getClass().getSimpleName(), service);
			beanFactory.registerSingleton(service.getClass().getSimpleName(), service);
		});

		// Refreshing the context
		taskContext.refresh();
		currentContext = taskContext;
	}

	private void startTask() {
		assert (currentClassName != null) && (currentContext != null);

		log.debug("Starting task from class {}.", currentClassName);

		taskLock.writeLock().lock();
		try {
			currentContext.start();
			currentTask = (Runnable)currentContext.getBean("runnable");

			log.info("Starting execution of {}.", currentTask);

			currentTaskFuture = executorService.schedule(currentTask, 0L, TimeUnit.SECONDS);
			Futures.addCallback(currentTaskFuture, new ExecutionListener());
		} finally {
			taskLock.writeLock().unlock();
		}
	}

	private void cleanUpAfterTask() {
		log.debug("Cleaning up after task {}.", currentTask);
		taskLock.writeLock().lock();
		try {
			currentClassName = null;
			currentTaskFuture = null;
			currentTask = null;

			if (currentContext != null) {
				currentContext.stop();
				currentContext.close();
				currentContext = null;
			}
		} finally {
			taskLock.writeLock().unlock();
		}
		log.debug("Clean up finished.");
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
