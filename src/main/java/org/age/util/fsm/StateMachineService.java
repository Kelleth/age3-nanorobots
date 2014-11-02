package org.age.util.fsm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import javax.annotation.concurrent.ThreadSafe;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.consumingIterable;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;

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
public abstract class StateMachineService<S extends Enum<S>, E extends Enum<E>> implements FSM<S, E> {

	private static final Logger log = LoggerFactory.getLogger(StateMachineService.class);

	private final String name;

	private final Table<S, E, TransitionDescriptor<S, E>> transitionsTable;

	private final E failureEvent;

	private final Consumer<List<Throwable>> exceptionHandler;

	private final List<Throwable> exceptions = newArrayListWithExpectedSize(2);

	private final ListeningScheduledExecutorService service;

	private final PriorityBlockingQueue<EventHolder> eventQueue = new PriorityBlockingQueue<>();

	private final EnumSet<S> terminalStates;

	private final Method eventCreate;

	private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

	private final ScheduledFuture<?> dispatcherFuture;

	private final boolean shutdownAfterTerminalState;

	private final EventBus eventBus;

	private final S initialState;

	@NonNull @GuardedBy("stateLock") private S currentState;

	@Nullable @GuardedBy("stateLock") private E currentEvent;

	@Nullable @GuardedBy("stateLock") private S nextState;

	/**
	 * Package-protected constructor.
	 * <p>
	 *
	 * The proper way to build the service is to use the builder {@link StateMachineServiceBuilder}.
	 *
	 * @param builder
	 * 		a builder containing the state machine definition.
	 */
	StateMachineService(@NonNull final StateMachineServiceBuilder<S, E> builder) throws NoSuchMethodException {
		name = builder.getName();
		initialState = builder.getInitialState();
		currentState = initialState;
		eventBus = builder.getEventBus();
		final Class<? extends StateChangedEvent> stateChangedEventClass = builder.getStateChangedEventClass();
		Method tmpMethod;
		try {
			tmpMethod = stateChangedEventClass.getMethod("create", builder.getStateClass(), builder.getEventClass(),
			                                             builder.getStateClass());
		} catch (final NoSuchMethodException e) {
			tmpMethod = stateChangedEventClass.getMethod("create", Enum.class, Enum.class, Enum.class);
		}
		log.debug("{}: Method: {}.", name, tmpMethod);
		eventCreate = tmpMethod;
		terminalStates = builder.getTerminalStates();
		shutdownAfterTerminalState = builder.getShutdownWhenTerminated();
		failureEvent = builder.getFailureEvent();
		exceptionHandler = builder.getExceptionHandler();
		transitionsTable = builder.buildTransitionsTable();
		service = listeningDecorator(newSingleThreadScheduledExecutor(
				new ThreadFactoryBuilder().setNameFormat("fsm-" + name + "-srv-%d").build()));
		dispatcherFuture = service.scheduleAtFixedRate(new Dispatcher(), 0, 1, TimeUnit.MILLISECONDS);
	}

	/**
	 * Fires a specific event.
	 *
	 * @param event
	 * 		an event to proceed with.
	 */
	public void fire(@NonNull final E event) {
		log.debug("{}: {} fired.", name, event);
		eventQueue.add(new EventHolder(event));
	}

	@Nullable
	private E getCurrentEvent() {
		return withReadLock(() -> currentEvent);
	}

	private void setCurrentEvent(@Nullable final E event) {
		withWriteLock(() -> currentEvent = event);
	}

	private void withWriteLock(@NonNull final Runnable runnable) {
		stateLock.writeLock().lock();
		try {
			runnable.run();
		} finally {
			stateLock.writeLock().unlock();
		}
	}

	@Nullable
	private <T> T withReadLock(@NonNull final Supplier<T> function) {
		stateLock.readLock().lock();
		try {
			return function.get();
		} finally {
			stateLock.readLock().unlock();
		}
	}

	// FSMk

	@Override
	public void goTo(@NonNull final S nextState) {
		this.nextState = nextState;
	}

	public boolean running() {
		return withReadLock(() -> !(inState(initialState) || terminated()));
	}

	/**
	 * Checks atomically whether the service is in the given state.
	 *
	 * @param state
	 * 		a state to check.
	 *
	 * @return true if the service is in the given state, false otherwise.
	 */
	public final boolean inState(@NonNull final S state) {
		return withReadLock(() -> currentState == state);
	}

	/**
	 * Checks whether this service is terminated.
	 *
	 * @return true if the FSM is terminated (in a terminal state).
	 */
	public boolean terminated() {
		return withReadLock(() -> terminalStates.contains(currentState));
	}

	/**
	 * Checks whether this service is currently terminating (transitioning to a terminal state).
	 *
	 * @return true if the FSM is terminating.
	 */
	public boolean isTerminating() {
		return withReadLock(() -> {
			if (currentEvent == null) {
				return false;
			}
			final TransitionDescriptor<S, E> descriptor = transitionsTable.get(currentState, currentEvent);
			return !Sets.intersection(terminalStates, descriptor.getTarget()).isEmpty();
		});
	}

	/**
	 * Shutdowns and cleans up the service.
	 *
	 * @throws IllegalStateException
	 * 		if service has not terminated yet.
	 * @see #terminated()
	 */
	public void shutdown() {
		checkState(terminated(), "Service has not terminated yet. Current state: %s.", getCurrentState());
		internalShutdown();
		log.info("{}: Service has been shut down properly.", name);
	}

	/**
	 * Gets the state the service is currently in.
	 *
	 * @return the current state of the service.
	 */
	@NonNull
	public final S getCurrentState() {
		return withReadLock(() -> currentState);
	}

	private void setCurrentState(@NonNull final S state) {
		withWriteLock(() -> currentState = state);
	}

	private void internalShutdown() {
		log.debug("{}: Service is shutting down.", name);
		dispatcherFuture.cancel(false);
		shutdownAndAwaitTermination(service, 10, TimeUnit.SECONDS);
	}

	@Override
	public String toString() {
		return withReadLock(() -> toStringHelper(this).addValue(name)
		                                              .add("S", currentState)
		                                              .add("E", currentEvent)
		                                              .add("terminated?", terminated())
		                                              .toString());
	}

	public void drainEvents() {
		for (final EventHolder holder : consumingIterable(eventQueue)) {
			log.warn("{}: Unprocessed event {}.", name, holder.getEvent());
			holder.getSemaphore().release();
		}
	}

	private class Dispatcher implements Runnable {
		@Override
		public void run() {
			try {
				if (terminated()) {
					if (!eventQueue.isEmpty()) {
						log.warn("{}: Service already terminated ({}).", name, currentState);
						drainEvents();
					}

					if (!service.isShutdown() && shutdownAfterTerminalState) {
						internalShutdown();
					}
					return;
				}

				if (getCurrentEvent() != null || eventQueue.isEmpty()) {
					return;
				}

				final EventHolder holder;
				final E event;
				stateLock.writeLock().lock();
				try {
					try {
						holder = eventQueue.take();
					} catch (final InterruptedException e) {
						log.debug("{}: Interrupted when waiting for event. Returning.", name, e);
						return;
					}
					event = holder.getEvent();
					setCurrentEvent(event);

				} finally {
					stateLock.writeLock().unlock();
				}

				log.debug("{}: In {} and processing {}.", name, currentState, event);
				final TransitionDescriptor<S, E> transitionDescriptor = transitionsTable.get(currentState, event);
				if (transitionDescriptor.isNull()) {
					log.debug("{}: Null transition.", name);
					fire(failureEvent);
					setCurrentEvent(null);
					holder.getSemaphore().release();
					return;
				}
				log.debug("{}: Planned transition: {}.", name, transitionDescriptor);

				final Consumer<FSM<S, E>> runnable = transitionDescriptor.getAction();
				try {
					runnable.accept(StateMachineService.this);
					onSuccess(transitionDescriptor);
				} catch (final Throwable t) {
					onFailure(transitionDescriptor, t);
				} finally {
					holder.getSemaphore().release();
				}
			} catch (final Throwable t) {
				log.error("Error", t);
			}
		}

		public void onSuccess(final TransitionDescriptor<S, E> descriptor) {
			withWriteLock(() -> {
				final Set<S> targetSet = descriptor.getTarget();
				if (targetSet.size() != 1 && nextState == null) {
					log.error("{}: Transition {} did not set the target state. Possible states: {}.", name, descriptor,
					          targetSet);
					fire(failureEvent);
				} else {
					if (targetSet.size() != 1 && nextState != null) {
						setCurrentState(nextState);
					} else {
						setCurrentState(Iterables.getOnlyElement(targetSet));
					}
					log.info("{}: Transition {} was successful. Selected state: {}.", name, descriptor,
					         getCurrentState());
					try {
						eventBus.post(eventCreate.invoke(null, descriptor.getInitial(), descriptor.getEvent(),
						                                 getCurrentState()));
					} catch (final IllegalAccessException | InvocationTargetException e) {
						log.error("{}: Cannot create event object.", name, e);
					}
				}
				setCurrentEvent(null);
				nextState = null;
			});
		}

		public void onFailure(final TransitionDescriptor<S, E> descriptor, final Throwable t) {
			log.error("{}: Transition {} failed with exception.", name, descriptor, t);
			setCurrentEvent(null);
			exceptions.add(t);
			fire(failureEvent);
		}
	}

	private final class EventHolder implements Comparable<EventHolder> {

		@NonNull private final E event;

		private final Semaphore semaphore = new Semaphore(0);

		public EventHolder(final E event) {
			assert event != null;
			this.event = event;
		}

		final E getEvent() {
			return event;
		}

		final Semaphore getSemaphore() {
			return semaphore;
		}

		@Override
		public final int compareTo(final EventHolder o) {
			if (failureEvent.equals(this.event) && failureEvent.equals(o.event)) {
				return 0;
			}
			if (failureEvent.equals(this.event) && !failureEvent.equals(o.event)) {
				return 1;
			}
			if (!failureEvent.equals(this.event) && !failureEvent.equals(o.event)) {
				return -1;
			}
			return 0;
		}

		@Override
		public String toString() {
			return toStringHelper(this).addValue(event).toString();
		}
	}

}
