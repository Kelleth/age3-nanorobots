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

package org.age.services.lifecycle.internal;

import static com.google.common.collect.Maps.newEnumMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.age.services.discovery.DiscoveryServiceStoppingEvent;
import org.age.services.lifecycle.LifecycleMessage;
import org.age.services.lifecycle.NodeDestroyedEvent;
import org.age.services.lifecycle.NodeLifecycleService;
import org.age.util.fsm.FSM;
import org.age.util.fsm.StateMachineService;
import org.age.util.fsm.StateMachineServiceBuilder;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class DefaultNodeLifecycleService implements SmartLifecycle, NodeLifecycleService {

	/**
	 * States of this lifecycle manager (in other words - states of the node).
	 *
	 * @author AGH AgE Team
	 */
	public enum State {
		/**
		 * Initial state of the node.
		 */
		OFFLINE,
		/**
		 * Node has been initialized.
		 */
		RUNNING,
		DISCONNECTED,
		/**
		 * Node has failed.
		 */
		FAILED,
		/**
		 * Node has terminated (terminal state).
		 */
		TERMINATED
	}

	/**
	 * Events that can occur in the node.
	 *
	 * @author AGH AgE Team
	 */
	public enum Event {
		/**
		 * Sent by the bootstrapper.
		 */
		START,
		CONNECTION_DOWN,
		RECONNECTED,
		DESTROY,
		/**
		 * Indicates that an error occurred.
		 */
		ERROR,
		/**
		 * Terminates the node.
		 */
		STOP
	}

	public static final String CHANNEL_NAME = "lifecycle/channel";

	private static final Logger log = LoggerFactory.getLogger(DefaultNodeLifecycleService.class);

	@Inject private @MonotonicNonNull HazelcastInstance hazelcastInstance;

	@Inject private @MonotonicNonNull EventBus eventBus;

	private @MonotonicNonNull ITopic<LifecycleMessage> topic;

	private @MonotonicNonNull StateMachineService<State, Event> service;

	private final EnumMap<LifecycleMessage.Type, Consumer<Serializable>> messageHandlers = newEnumMap(
			LifecycleMessage.Type.class);

	public DefaultNodeLifecycleService() {
		messageHandlers.put(LifecycleMessage.Type.DESTROY, this::handleDestroy);
	}

	@PostConstruct private void construct() {
		//@formatter:off
		service = StateMachineServiceBuilder
			.withStatesAndEvents(State.class, Event.class)
			.withName("lifecycle")
			.startWith(State.OFFLINE)
			.terminateIn(State.TERMINATED, State.FAILED)

			.in(State.OFFLINE)
				.on(Event.START).execute(this::internalStart).goTo(State.RUNNING)
				.commit()

			.in(State.RUNNING)
				.on(Event.CONNECTION_DOWN).execute(this::connectionDown).goTo(State.DISCONNECTED)
				.commit()

			.in(State.DISCONNECTED)
				.on(Event.RECONNECTED).execute(this::reconnected).goTo(State.RUNNING)
				.commit()

			.inAnyState()
				.on(Event.DESTROY).execute(this::destroy).goTo(State.TERMINATED)
				.on(Event.STOP).execute(this::internalStop).goTo(State.TERMINATED)
				.on(Event.ERROR).execute(fsm -> log.debug("ERROR")).goTo(State.FAILED)
				.commit()

			.ifFailed()
				.fireAndCall(Event.ERROR, new ExceptionHandler())

			.withEventBus(eventBus)
			.build();
		//@formatter:on

		topic = hazelcastInstance.getTopic(CHANNEL_NAME);
	}

	@Override public boolean isAutoStartup() {
		return true;
	}

	@Override public void stop(final Runnable callback) {
		stop();
		callback.run();
	}

	@Override public void start() {
		log.debug("Node lifecycle service starting.");
		service.fire(Event.START);
	}

	@Override public void stop() {
		log.debug("Node lifecycle service stopping.");
		service.fire(Event.STOP);
		// The context must wait till the termination process will have finished
		try {
			awaitTermination();
		} catch (final InterruptedException ignored) {
			Thread.interrupted();
		}
		log.info("Node lifecycle service stopped.");
	}

	@Override public boolean isRunning() {
		return !(service.isInState(State.OFFLINE) || service.isTerminated());
	}

	@Override public int getPhase() {
		return Integer.MIN_VALUE;
	}

	@Override public void awaitTermination() throws InterruptedException {
		log.debug("Awaiting termination.");
		service.awaitTermination();
	}

	@Override public boolean isTerminated() {
		return service.isTerminated();
	}

	// Transitions

	private void internalStart(final @NonNull FSM<State, Event> fsm) {
		log.debug("Node lifecycle service starting.");

		topic.addMessageListener(new DistributedMessageListener());
		eventBus.register(this);

		log.info("Node lifecycle service started.");
	}

	private void internalStop(final @NonNull FSM<State, Event> fsm) {
		log.debug("Node lifecycle service stopping.");

		log.info("Node lifecycle service stopped.");
	}

	private void connectionDown(final @NonNull FSM<State, Event> fsm) {
		log.debug("Connection down.");
	}

	private void reconnected(final @NonNull FSM<State, Event> fsm) {
		log.debug("Reconnected.");
	}

	private void destroy(final @NonNull FSM<State, Event> fsm) {
		log.info("Destroying the node.");
		eventBus.post(new NodeDestroyedEvent());
	}

	// Message handling

	private void handleDestroy(final @Nullable Serializable serializable) {
		assert isNull(serializable);
		log.debug("Destroy message received.");
		service.fire(Event.DESTROY);
	}

	// Listeners

	@Subscribe private void handleDiscoveryServiceStoppingEvent(final @NonNull DiscoveryServiceStoppingEvent event) {
		log.debug("Discovery service is stopping.");
		service.fire(Event.CONNECTION_DOWN);
	}

	private class DistributedMessageListener implements MessageListener<LifecycleMessage> {
		@Override public void onMessage(final Message<LifecycleMessage> message) {
			log.debug("Distributed event: {}.", message);
			final LifecycleMessage lifecycleMessage = message.getMessageObject();
			log.debug("Lifecycle message: {}.", lifecycleMessage);
			messageHandlers.get(lifecycleMessage.getType()).accept(lifecycleMessage.getPayload().orElse(null));
		}
	}

	private class ExceptionHandler implements Consumer<Throwable> {
		@Override public void accept(final @NonNull Throwable throwable) {
			assert nonNull(throwable);
			log.error("Node lifecycle service error.", throwable);
		}
	}
}
