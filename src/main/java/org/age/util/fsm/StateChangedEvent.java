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
import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An event describing a FSM state change.
 *
 * @param <S>
 * 		a states type.
 * @param <E>
 * 		an events type
 *
 * @author AGH AgE Team
 */
@Immutable
public class StateChangedEvent<S extends Enum<S>, E extends Enum<E>> {

	private final S previousState;

	private final E event;

	private final S newState;

	private final LocalDateTime timestamp = LocalDateTime.now();

	/**
	 * Creates a new event.
	 *
	 * @param previousState
	 * 		a previous state.
	 * @param event
	 * 		an event that caused the transition.
	 * @param newState
	 * 		a new state.
	 */
	protected StateChangedEvent(@NonNull final S previousState, @NonNull final E event, @NonNull final S newState) {
		this.previousState = requireNonNull(previousState);
		this.event = requireNonNull(event);
		this.newState = requireNonNull(newState);
	}

	/**
	 * Creates a new event.
	 *
	 * @param <S>
	 * 		a states type.
	 * @param <E>
	 * 		an events type
	 * @param previousState
	 * 		a previous state.
	 * @param event
	 * 		an event that caused the transition.
	 * @param newState
	 * 		a new state.
	 *
	 * @return a new event.
	 */
	@NonNull public static <S extends Enum<S>, E extends Enum<E>> StateChangedEvent<S, E> create(
			@NonNull final S previousState, @NonNull final E event, @NonNull final S newState) {
		return new StateChangedEvent<>(previousState, event, newState);
	}

	/**
	 * Returns the previous state.
	 */
	@NonNull public S previousState() {
		return previousState;
	}

	/**
	 * Returns the event that caused the transition.
	 */
	@NonNull public E event() {
		return event;
	}

	/**
	 * Returns the new state.
	 */
	@NonNull public S newState() {
		return newState;
	}

	/**
	 * Returns the timestamp of the event.
	 */
	@NonNull public LocalDateTime timestamp() {
		return timestamp;
	}

	@Override public String toString() {
		return toStringHelper(this).add("previous", previousState).add("event", event).add("new", newState).toString();
	}

	@Override public boolean equals(final Object obj) {
		if (!(obj instanceof StateChangedEvent)) {
			return false;
		}
		final StateChangedEvent<?, ?> other = (StateChangedEvent)obj;

		return Objects.equals(previousState, other.previousState) && Objects.equals(event, other.event)
		       && Objects.equals(newState, other.newState) && Objects.equals(timestamp, other.timestamp);
	}

	@Override public int hashCode() {
		return Objects.hash(previousState, event, newState, timestamp);
	}
}
