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

import org.age.services.lifecycle.NodeLifecycleService;
import org.age.util.fsm.StateMachineService;
import org.age.util.fsm.StateMachineServiceBuilder;

import com.google.common.eventbus.EventBus;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.TimeUnit;
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
		STARTED,
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
		/**
		 * Indicates that an error occurred.
		 */
		ERROR,
		/**
		 * Terminates the node.
		 */
		STOP
	}

	private static final Logger log = LoggerFactory.getLogger(DefaultNodeLifecycleService.class);

	@Inject @MonotonicNonNull private EventBus eventBus;

	@MonotonicNonNull private StateMachineService<State, Event> service;

	@PostConstruct public final void construct() {
		//@formatter:off
		service = StateMachineServiceBuilder
			.withStatesAndEvents(State.class, Event.class)
			.withName("lifecycle")
			.startWith(State.OFFLINE)
			.terminateIn(State.TERMINATED)

			.in(State.OFFLINE)
				.on(Event.START).execute(fsm -> log.debug("START")).goTo(State.STARTED)
				.commit()

			.inAnyState()
				.on(Event.STOP).execute(fsm -> log.debug("STOP")).goTo(State.TERMINATED)
				.on(Event.ERROR).execute(fsm -> log.debug("ERROR")).goTo(State.FAILED)
				.commit()

			.ifFailed()
				.fireAndCall(Event.ERROR, new ExceptionHandler())

			.withEventBus(eventBus)
			//.notifyWithType(LifecycleStateChangedEvent.class)
			.build();
		//@formatter:on
	}

	@Override public boolean isAutoStartup() {
		return true;
	}

	@Override public void stop(final Runnable runnable) {
		stop();
		runnable.run();
	}

	public void awaitTermination() {
		log.debug("Awaiting termination.");
		// XXX: It should be nicer.
		while (!service.isTerminated()) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override public void start() {
		log.debug("Node lifecycle service starting.");
		service.fire(Event.START);
	}

	@Override public void stop() {
		log.debug("Node lifecycle service stopping.");
		service.fire(Event.STOP);
		awaitTermination();
		log.info("Node lifecycle service stopped.");
	}

	@Override public boolean isRunning() {
		return !(service.isInState(State.OFFLINE) || service.isTerminated());
	}

	@Override public int getPhase() {
		return Integer.MIN_VALUE;
	}

	private class ExceptionHandler implements Consumer<Throwable> {

		@Override public void accept(final Throwable throwable) {

		}
	}
}
