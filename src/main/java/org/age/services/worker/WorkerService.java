/*
 * Created: 2014-08-25
 * $Id$
 */

package org.age.services.worker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.age.compute.api.BroadcastMessenger;
import org.age.services.topology.TopologyService;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;

public class WorkerService implements SmartLifecycle {

	public static final String CHANNEL_NAME = "worker/channel";

	private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final ListeningScheduledExecutorService executorService = listeningDecorator(
			newSingleThreadScheduledExecutor());

	@MonotonicNonNull @Inject private HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull TopologyService topologyService;

	@MonotonicNonNull private ITopic<WorkerMessage> topic;

	@MonotonicNonNull @Inject private EventBus eventBus;

	@Nullable private String currentClassName;

	@Nullable private Runnable currentTask;

	@Nullable private AnnotationConfigApplicationContext currentContext;

	@Nullable private ListenableScheduledFuture<?> currentTaskFuture;

	@Inject private ApplicationContext applicationContext;

	private BroadcastMessenger broadcastMessenger;

	@PostConstruct
	public void construct() {
		topic = hazelcastInstance.getTopic(CHANNEL_NAME);

		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);
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

	@Override
	public void start() {
		log.debug("Worker service starting.");

		running.set(true);

		// Creating services
		broadcastMessenger = applicationContext.getBean(BroadcastMessenger.class);

		log.info("Worker service started.");
	}

	@Override
	public void stop() {
		log.debug("Worker service stopping.");

		running.set(false);
		shutdownAndAwaitTermination(executorService, 10, TimeUnit.SECONDS);

		log.info("Worker service stopped.");
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	public boolean isTaskRunning() {
		return currentTaskFuture != null && !currentTaskFuture.isDone();
	}

	private void setupTask(@NonNull final String className) {
		try {
			log.debug("Setting up task from class {}.", className);
			currentClassName = className;

			currentContext = new AnnotationConfigApplicationContext();
			final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(className);
			currentContext.registerBeanDefinition("runnable", builder.getBeanDefinition());

			// Add services
			log.info("Broadcast messenger: {}.", broadcastMessenger);
			currentContext.getBeanFactory().registerSingleton("broadcastMessenger", broadcastMessenger);
			log.info("{}", currentContext.getBeanFactory().isTypeMatch("broadcastMessenger", BroadcastMessenger.class));

			currentContext.refresh();

			log.debug("Task set up.");
		} catch (final BeanCreationException e) {
			log.error("Cannot create the task.", e);
		}
	}

	private void startTask() {
		log.debug("Starting task from class {}.", currentClassName);

		currentContext.start();
		currentTask = (Runnable)currentContext.getBean("runnable");

		log.info("Starting execution of {}.", currentTask);

		currentTaskFuture = executorService.schedule(currentTask, 0, TimeUnit.SECONDS);
		Futures.addCallback(currentTaskFuture, new ExecutionListener());
	}

	private void cleanUpAfterTask() {
		log.debug("Cleaning up after task {}.", currentTask);
		currentClassName = null;
		currentTaskFuture = null;
		currentTask = null;

		currentContext.stop();
		currentContext.close();
	}

	private class DistributedMessageListener implements MessageListener<WorkerMessage> {
		@Override
		public void onMessage(final Message<WorkerMessage> message) {
			final WorkerMessage messageObject = message.getMessageObject();
			log.debug("WorkerMessage received: {}.", messageObject);

			switch (messageObject.getType()) {
				case LOAD_CLASS:
					final String className = (String)messageObject.getPayload().get();
					setupTask(className);
					startTask();
					break;
			}
		}
	}

	private class ExecutionListener implements FutureCallback<Object> {

		@Override
		public void onSuccess(final Object result) {
			log.info("Task {} finished.", currentTask);
			cleanUpAfterTask();
		}

		@Override
		public void onFailure(final Throwable t) {
			log.error("Task {} failed with error.", currentTask, t);
			cleanUpAfterTask();
		}
	}
}
