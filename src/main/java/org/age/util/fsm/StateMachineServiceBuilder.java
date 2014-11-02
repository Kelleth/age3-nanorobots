/**
 * Copyright (C) 2006 - 2012
 *   Pawel Kedzior
 *   Tomasz Kmiecik
 *   Kamil Pietak
 *   Krzysztof Sikora
 *   Adam Wos
 *   Lukasz Faber
 *   Daniel Krzywicki
 *   and other students of AGH University of Science and Technology.
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
 * Created: 2012-08-21
 * $Id: 53954ddb2f07014defed684e02246fe0ee1a1afa $
 */

package org.age.util.fsm;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newEnumMap;

/**
 * A builder of {@link StateMachineService} instances. It offers a simple, flexible interface for creation of state
 * machines.
 * <p>
 *
 * Initially, a user is required to provide at least:
 * <ul>
 * <li> an enumeration of states,
 * <li> an enumeration of transitions,
 * <li> an entry state ({@link #startWith}),
 * <li> terminal states ({@link #terminateIn}),
 * <li> an event to fire on failures and errors ({@link #ifFailed}).
 * </ul>
 * Failure to do so results in {@link IllegalStateException} when {@link #build} is called.
 *
 * @param <S>
 * 		the states enumeration.
 * @param <E>
 * 		the events enumeration.
 *
 * @author AGH AgE Team
 */
public class StateMachineServiceBuilder<S extends Enum<S>, E extends Enum<E>> {

	private static final Logger log = LoggerFactory.getLogger(StateMachineServiceBuilder.class);

	private final FailureBehaviorBuilder failureBehaviorBuilder = new FailureBehaviorBuilder();

	private final Table<S, E, Set<S>> transitions;

	private final Map<E, Set<S>> noStateTransitions;

	private final Table<S, E, Consumer<FSM<S, E>>> actions;

	private final Map<E, Consumer<FSM<S, E>>> noStateActions;

	private final Class<S> stateClass;

	private final Class<E> eventClass;

	private S initialState;

	private EnumSet<S> terminalStates;

	private EventBus eventBus;

	private String name;

	private boolean shutdownWhenTerminated;

	private Class<? extends StateChangedEvent> stateChangedEventClass = StateChangedEvent.class;

	StateMachineServiceBuilder(@NonNull final Class<S> states, @NonNull final Class<E> events) {
		stateClass = requireNonNull(states);
		eventClass = requireNonNull(events);

		transitions = ArrayTable.create(EnumSet.allOf(stateClass), EnumSet.allOf(eventClass));
		actions = ArrayTable.create(EnumSet.allOf(stateClass), EnumSet.allOf(eventClass));

		noStateTransitions = newEnumMap(eventClass);
		noStateActions = newEnumMap(eventClass);
	}

	public static <S extends Enum<S>, E extends Enum<E>> StateMachineServiceBuilder<S, E> withStatesAndEvents(
			@NonNull final Class<S> states, @NonNull final Class<E> events) {
		return new StateMachineServiceBuilder<>(states, events);
	}

	/**
	 * Requests that events will be sent synchronously.
	 *
	 * @return this builder instance.
	 */
	public StateMachineServiceBuilder<S, E> notifyWithType(Class<? extends StateChangedEvent<S, E>> stateChangedEventClass) {
		this.stateChangedEventClass = stateChangedEventClass;
		return this;
	}

	public StateMachineServiceBuilder<S, E> withName(@NonNull final String name) {
		this.name = requireNonNull(name);
		return this;
	}

	/**
	 * Starts the declaration of behaviour when the FSM is at the given state.
	 *
	 * @param state
	 * 		a state.
	 *
	 * @return an action builder.
	 */
	public ActionBuilder in(final S state) {
		return new ActionBuilder(state);
	}

	/**
	 * Starts the declaration of behaviour for the events that are not dependent on states.
	 *
	 * @return an action builder.
	 */
	public AnyStateActionBuilder inAnyState() {
		return new AnyStateActionBuilder();
	}

	/**
	 * Declares an initialState state.
	 *
	 * @param state
	 * 		a state.
	 *
	 * @return this builder instance.
	 */
	public StateMachineServiceBuilder<S, E> startWith(final S state) {
		initialState = requireNonNull(state);
		log.debug("Starting state: {}.", initialState);
		return this;
	}

	/**
	 * Indicates which states are terminalStates.
	 *
	 * @param states
	 * 		states that should be marked as terminalStates.
	 *
	 * @return this builder instance.
	 */
	public StateMachineServiceBuilder<S, E> terminateIn(final S... states) {
		checkArgument(states.length > 0, "Must provide at least one terminating state.");

		terminalStates = EnumSet.copyOf(Arrays.asList(states));
		log.debug("Terminal states: {}.", terminalStates);
		return this;
	}

	/**
	 * Starts the declaration of actions taken when the failure occurs.
	 *
	 * @return a failure behaviour builder.
	 */
	public FailureBehaviorBuilder ifFailed() {
		return failureBehaviorBuilder;
	}

	/**
	 * Schedules FSM shutdown after a terminalStates state has been reached.
	 *
	 * @return this builder instance.
	 */
	public StateMachineServiceBuilder<S, E> shutdownWhenTerminated() {
		shutdownWhenTerminated = true;
		return this;
	}

	public StateMachineServiceBuilder<S, E> withEventBus(final EventBus eventBus) {
		this.eventBus = eventBus;
		return this;
	}

	/**
	 * Builds and returns a new service.
	 *
	 * @return a new {@code StateMachineService}.
	 */
	public StateMachineService<S, E> build() {
		log.debug("Building a state machine: N={}, S={}, E={}.", name, stateClass, eventClass);
		checkState(name != null);
		checkState(stateClass != null);
		checkState(eventClass != null);
		checkState(initialState != null);
		checkState(terminalStates != null);
		checkState(getFailureEvent() != null);

		try {
			return new StateMachineService<S, E>(this) {
				/* Empty */
			};
		} catch (final NoSuchMethodException e) {
			log.error("Incorrect event class.", e);
			throw new RuntimeException(e);
		}
	}

	// Package-protected methods for service creation and testing

	@NonNull String getName() {
		return name;
	}

	@NonNull Table<S, E, Set<S>> getTransitions() {
		return transitions;
	}

	@NonNull Table<S, E, Consumer<FSM<S, E>>> getActions() {
		return actions;
	}

	@Nullable S getInitialState() {
		return initialState;
	}

	@NonNull FailureBehaviorBuilder getFailureBehaviorBuilder() {
		return failureBehaviorBuilder;
	}

	EnumSet<S> getTerminalStates() {
		return terminalStates;
	}

	EventBus getEventBus() {
		return eventBus;
	}

	boolean getShutdownWhenTerminated() {
		return shutdownWhenTerminated;
	}

	@Nullable Map<E, Set<S>> getAnyTransitions() {
		return noStateTransitions;
	}

	@Nullable Map<E, Consumer<FSM<S, E>>> getAnyActions() {
		return noStateActions;
	}

	Class<? extends StateChangedEvent> getStateChangedEventClass() {
		return stateChangedEventClass;
	}

	@NonNull E getFailureEvent() {
		return failureBehaviorBuilder.getEvent();
	}

	@NonNull Consumer<List<Throwable>> getExceptionHandler() {
		return failureBehaviorBuilder.getFunction();
	}

	/**
	 * Builds the transitions table.
	 *
	 * @return an immutable transitions table.
	 */
	ImmutableTable<S, E, TransitionDescriptor<S, E>> buildTransitionsTable() {
		final EnumSet<S> allStates = EnumSet.allOf(stateClass);
		final EnumSet<E> allEvents = EnumSet.allOf(eventClass);
		final Table<S, E, TransitionDescriptor<S, E>> table = ArrayTable.create(allStates, allEvents);

		for (final S state : allStates) {
			for (final E event : allEvents) {
				table.put(state, event, TransitionDescriptor.getNull());
			}
		}

		for (final S state : allStates) {
			noStateTransitions.forEach((event, targetStates) -> {
				final TransitionDescriptor<S, E> descriptor = new TransitionDescriptor<>(state, event, targetStates,
				                                                                         noStateActions.get(event));
				table.put(state, event, descriptor);
			});
			transitions.row(state).forEach((event, targetStates) -> {
				if (targetStates == null) {
					return;
				}
				final TransitionDescriptor<S, E> descriptor = new TransitionDescriptor<>(state, event, targetStates,
				                                                                         actions.get(state, event));
				table.put(state, event, descriptor);
			});
		}

		if (log.isDebugEnabled()) {
			table.values().forEach(descriptor -> {
				if (!descriptor.isNull()) {
					log.debug("New transition: {}.", descriptor);
				}
			});
		}

		return ImmutableTable.copyOf(table);
	}

	@NonNull Class<S> getStateClass() {
		return stateClass;
	}

	@NonNull Class<E> getEventClass() {
		return eventClass;
	}

	/**
	 * An action builder.
	 *
	 * @author AGH AgE Team
	 */
	public final class ActionBuilder {

		private final S entry;

		@Nullable private E event;

		@Nullable private Set<S> exit;

		@Nullable private Consumer<FSM<S, E>> action;

		private ActionBuilder(@NonNull final S entry) {
			assert entry != null;
			this.entry = entry;
		}

		/**
		 * Declares an event that causes the action.
		 *
		 * @param initiatingEvent
		 * 		a causing event.
		 *
		 * @return this action builder.
		 */
		@NonNull
		public ActionBuilder on(@NonNull final E initiatingEvent) {
			requireNonNull(initiatingEvent);
			if (event != null) {
				checkState(exit != null, "Declaring new event without configuring previous.");
				transitions.put(entry, event, exit);
				actions.put(entry, event, action);
				event = null;
				exit = null;
				action = null;
			}
			event = initiatingEvent;
			return this;
		}

		/**
		 * Declares an action to be executed during transition.
		 *
		 * @param actionToExecute
		 * 		an action to execute.
		 *
		 * @return this action builder.
		 */
		@NonNull
		public ActionBuilder execute(@NonNull final Consumer<FSM<S, E>> actionToExecute) {
			action = requireNonNull(actionToExecute);
			return this;
		}

		/**
		 * Declares a target state.
		 *
		 * @param state
		 * 		a target state.
		 *
		 * @return this action builder.
		 */
		@SafeVarargs @NonNull
		public final ActionBuilder goTo(final S... state) {
			requireNonNull(state);
			checkArgument(state.length > 0, "Empty set of targets.");

			exit = ImmutableSet.copyOf(state);
			return this;
		}

		/**
		 * Finishes the action declaration.
		 *
		 * @return a state machine builder.
		 */
		@NonNull
		public StateMachineServiceBuilder<S, E> commit() {
			checkState(event != null, "Event not provided.");
			checkState(exit != null, "Transition targets not provided.");
			checkState(!exit.isEmpty(), "Transition targets not provided.");

			transitions.put(entry, event, exit);
			actions.put(entry, event, action);
			return StateMachineServiceBuilder.this;
		}
	}

	/**
	 * An action builder for state-independent actions.
	 *
	 * @author AGH AgE Team
	 */
	public class AnyStateActionBuilder {

		@Nullable private E event;

		@Nullable private Set<S> exit;

		@Nullable private Consumer<FSM<S, E>> action;

		/**
		 * Declares an event that causes the action.
		 *
		 * @param initiatingEvent
		 * 		a causing event.
		 *
		 * @return this action builder.
		 */
		@NonNull
		public AnyStateActionBuilder on(final E initiatingEvent) {
			requireNonNull(initiatingEvent);
			if (event != null) {
				checkState(exit != null, "Declaring new event without configuring previous.");
				noStateTransitions.put(event, exit);
				noStateActions.put(event, action);
				event = null;
				exit = null;
				action = null;
			}
			event = initiatingEvent;
			return this;
		}

		/**
		 * Declares an action to be executed during transition.
		 *
		 * @param actionToExecute
		 * 		an action to execute.
		 *
		 * @return this action builder.
		 */
		@NonNull
		public AnyStateActionBuilder execute(final Consumer<FSM<S, E>> actionToExecute) {
			action = requireNonNull(actionToExecute);
			return this;
		}

		/**
		 * Declares a target state.
		 *
		 * @param state
		 * 		a target state.
		 *
		 * @return this action builder.
		 */
		@SafeVarargs @NonNull
		public final AnyStateActionBuilder goTo(final S... state) {
			requireNonNull(state);
			checkArgument(state.length > 0, "Empty set of targets.");

			exit = ImmutableSet.copyOf(state);
			return this;
		}

		/**
		 * Finishes the action declaration.
		 *
		 * @return a state machine builder.
		 */
		@NonNull
		public StateMachineServiceBuilder<S, E> commit() {
			checkState(event != null, "Event not provided.");
			checkState(exit != null, "Transition targets not provided.");
			checkState(!exit.isEmpty(), "Transition targets not provided.");

			noStateTransitions.put(event, exit);
			noStateActions.put(event, action);
			return StateMachineServiceBuilder.this;
		}
	}

	/**
	 * A builder for internal FSM failure.
	 *
	 * @author AGH AgE Team
	 */
	public class FailureBehaviorBuilder {

		@MonotonicNonNull private E event;

		@MonotonicNonNull private Consumer<List<Throwable>> function;

		/**
		 * Declares which event should be fired when failure occurs.
		 *
		 * @param eventToFire
		 * 		an event to fire.
		 *
		 * @return a state machine builder.
		 */
		@NonNull
		public StateMachineServiceBuilder<S, E> fireAndCall(@NonNull final E eventToFire,
		                                                    @NonNull final Consumer<List<Throwable>> exceptionHandler) {

			event = requireNonNull(eventToFire);
			function = requireNonNull(exceptionHandler);
			return StateMachineServiceBuilder.this;
		}

		@Nullable E getEvent() {
			return event;
		}

		@Nullable Consumer<List<Throwable>> getFunction() {
			return function;
		}
	}

}
