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

package org.age.util.fsm;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.consumingIterable;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Queues.newConcurrentLinkedQueue;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.age.util.Runnables.swallowingRunnable;

import org.age.annotation.ForTestsOnly;

import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A FSM-based service implementation.
 * <p>
 * These services should be built with {@link StateMachineServiceBuilder}.
 *
 * @param <S>
 * 		the states enumeration.
 * @param <E>
 * 		the events enumeration.
 *
 * @author AGH AgE Team
 * @see StateMachineServiceBuilder
 */
@ThreadSafe
public final class DefaultStateMachineService<S extends Enum<S>, E extends Enum<E>>
		implements StateMachineService<S, E> {

	private static final Logger log = LoggerFactory.getLogger(DefaultStateMachineService.class);

	private final String serviceName;

	private final EnumSet<S> terminalStates;

	private final Method eventCreate;

	private final S initialState;

	private final E failureEvent;

	private final boolean synchronous;

	private final Table<S, E, TransitionDescriptor<S, E>> transitionsTable;

	private final Consumer<Throwable> exceptionHandler;

	private final ConcurrentLinkedQueue<Throwable> exceptions = newConcurrentLinkedQueue();

	@Nullable private final ListeningScheduledExecutorService service;

	private final Queue<E> eventQueue = newConcurrentLinkedQueue();

	@Nullable private final ScheduledFuture<?> dispatcherFuture;

	private final EventBus eventBus;

	private final AtomicBoolean failed = new AtomicBoolean(false);

	private final StampedLock stateLock = new StampedLock();

	@NonNull @GuardedBy("stateLock") private S currentState;

	@Nullable @GuardedBy("stateLock") private E currentEvent;

	@Nullable @GuardedBy("stateLock") private S nextState;

	/**
	 * Package-protected constructor.
	 * <p>
	 * <p>
	 * The proper way to build the service is to use the builder {@link StateMachineServiceBuilder}.
	 *
	 * @param builder
	 * 		a builder containing the state machine definition.
	 */
	DefaultStateMachineService(@NonNull final StateMachineServiceBuilder<S, E> builder) {
		serviceName = builder.name();
		initialState = builder.initialState();
		currentState = initialState;
		eventBus = builder.eventBus();
		eventCreate = builder.stateChangedEventCreateMethod();
		terminalStates = builder.terminalStates();
		failureEvent = builder.getFailureEvent();
		exceptionHandler = builder.getExceptionHandler();
		transitionsTable = builder.buildTransitionsTable();
		if (builder.isSynchronous()) {
			synchronous = true;
			service = null;
			dispatcherFuture = null;
		} else {
			synchronous = false;
			service = listeningDecorator(newSingleThreadScheduledExecutor(
					new ThreadFactoryBuilder().setNameFormat("fsm-" + serviceName + "-srv-%d").build()));
			dispatcherFuture = service.scheduleWithFixedDelay(swallowingRunnable(new Dispatcher()), 0L, 1L,
			                                                  TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Fires an event.
	 * <p>
	 * If the event is a failure event, the FSM is marked as failed and cannot process events anymore.
	 *
	 * @param event
	 * 		an event to proceed with.
	 */
	@Override public void fire(@NonNull final E event) {
		log.debug("{}: {} fired.", serviceName, event);
		logIfTerminated();
		if (failureEvent == event) {
			log.debug("{}: Failure.", serviceName);
			failed.set(true);
		} else {
			eventQueue.add(event);
		}
	}

	@Override public void goTo(@NonNull final S nextState) {
		final long stamp = stateLock.writeLock();
		try {
			this.nextState = nextState;
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Override public boolean isRunning() {
		final long stamp = stateLock.readLock();
		try {
			return (currentState != initialState) && !terminalStates.contains(currentState);
		} finally {
			stateLock.unlock(stamp);
		}
	}


	@Override public boolean isInState(@NonNull final S state) {
		final long stamp = stateLock.readLock();
		try {
			return currentState == state;
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Override public boolean isTerminated() {
		final long stamp = stateLock.readLock();
		try {
			return terminalStates.contains(currentState);
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Override public void shutdown() {
		checkState(isTerminated(), "Service has not terminated yet. Current state: %s.", currentState());
		log.debug("{}: Service is in terminal state - performing shutdown.", serviceName);
		internalShutdown();
	}

	@Override public void forceShutdown() {
		log.debug("{}: Performing force shutdown.", serviceName);
		internalShutdown();
	}

	@Override @NonNull public S currentState() {
		final long stamp = stateLock.readLock();
		try {
			return currentState;
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Override public boolean isFailed() {
		final long stamp = stateLock.readLock();
		try {
			return failed.get();
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Nullable public E currentEvent() {
		final long stamp = stateLock.readLock();
		try {
			return currentEvent;
		} finally {
			stateLock.unlock(stamp);
		}
	}

	@Override public String toString() {
		final long stamp = stateLock.readLock();
		try {
			return toStringHelper(this).addValue(serviceName)
			                           .add("S", currentState)
			                           .add("E", currentEvent)
			                           .add("failed?", failed)
			                           .add("terminated?", terminalStates.contains(currentState))
			                           .toString();
		} finally {
			stateLock.unlock(stamp);
		}
	}

	void drainEvents() {
		for (final E event : consumingIterable(eventQueue)) {
			log.warn("{}: Unprocessed event {}.", serviceName, event);
		}
	}

	@ForTestsOnly void execute() {
		assert synchronous;
		new Dispatcher().run();
	}

	private void internalShutdown() {
		log.debug("{}: Service is shutting down.", serviceName);
		if (!synchronous) {
			assert (dispatcherFuture != null) && (service != null);
			dispatcherFuture.cancel(false);
			shutdownAndAwaitTermination(service, 10L, TimeUnit.SECONDS);
		}
		drainEvents();
		log.info("{}: Service has been shut down properly.", serviceName);
	}

	private void logIfTerminated() {
		final long stamp = stateLock.readLock();
		try {
			if (terminalStates.contains(currentState)) {
				log.warn("{}: Service already terminated ({}).", serviceName, currentState);
			}
		} finally {
			stateLock.unlock(stamp);
		}
	}

	private final class Dispatcher implements Runnable {
		@Override public void run() {
			if (isTerminated()) {
				return;
			}

			final TransitionDescriptor<S, E> transitionDescriptor;
			long stamp = stateLock.readLock();
			try {
				// Still processing previous event
				if (nonNull(currentEvent)) {
					return;
				}
				final long writeStamp = stateLock.tryConvertToWriteLock(stamp);
				if (writeStamp == 0L) {
					stateLock.unlockRead(stamp);
					stamp = stateLock.writeLock();
				} else {
					stamp = writeStamp;
				}
				assert stamp != 0L;

				// Prepare the current event
				if (failed.get()) {
					currentEvent = failureEvent;
				} else if (eventQueue.isEmpty()) {
					// Nothing to process
					return;
				} else {
					currentEvent = eventQueue.poll();
				}
				// Process the current event
				log.debug("{}: In {} and processing {}.", serviceName, currentState, currentEvent);
				transitionDescriptor = transitionsTable.get(currentState, currentEvent);
			} finally {
				stateLock.unlock(stamp);
			}

			log.debug("{}: Planned transition: {}.", serviceName, transitionDescriptor);

			// Execute the action
			try {
				final Consumer<FSM<S, E>> action = transitionDescriptor.action();
				log.debug("{}: Executing the planned action {}.", serviceName, action);
				action.accept(DefaultStateMachineService.this);
				log.debug("{}: Finished the execution of the action.", serviceName);
				onSuccess(transitionDescriptor);
			} catch (final Throwable t) {
				onFailure(transitionDescriptor, t);
			}

			consumingIterable(exceptions).forEach(exceptionHandler::accept);
		}

		public void onSuccess(final TransitionDescriptor<S, E> descriptor) {
			final long stamp = stateLock.writeLock();
			try {
				final Set<S> targetSet = descriptor.target();
				if ((targetSet.size() != 1) && (nextState == null)) {
					log.error("{}: Transition {} did not set the target state. Possible states: {}.", serviceName,
					          descriptor, targetSet);
					failed.set(true);
				} else {
					currentState = ((targetSet.size() != 1) && (nextState != null)) ? nextState
					                                                                : getOnlyElement(targetSet);
					log.info("{}: Transition {} was successful. Selected state: {}.", serviceName, descriptor,
					         currentState);
					eventBus.post(eventCreate.invoke(null, descriptor.initial(), descriptor.event(), currentState));
				}
			} catch (final IllegalAccessException | InvocationTargetException e) {
				log.error("{}: Cannot create event object.", serviceName, e);
			} finally {
				currentEvent = null;
				nextState = null;
				stateLock.unlock(stamp);
			}
		}

		public void onFailure(final TransitionDescriptor<S, E> descriptor, final Throwable t) {
			final long stamp = stateLock.writeLock();
			try {
				log.error("{}: Transition {} failed with exception.", serviceName, descriptor, t);
				exceptions.add(t);
				failed.set(true);
			} finally {
				currentEvent = null;
				nextState = null;
				stateLock.unlock(stamp);
			}
		}
	}

}
