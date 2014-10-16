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
 * $Id: 2ada32d15bf8c646e66d445130a7ba8e4409c9a5 $
 */

package org.age.util.fsm;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.MoreObjects.toStringHelper;

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
	protected StateChangedEvent(final S previousState, final E event, final S newState) {
		this.previousState = previousState;
		this.event = event;
		this.newState = newState;
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
	public static <S extends Enum<S>, E extends Enum<E>> StateChangedEvent<S, E> create(final S previousState,
	                                                                                    final E event,
	                                                                                    final S newState) {
		return new StateChangedEvent<>(previousState, event, newState);
	}

	/**
	 * Returns the previous state.
	 *
	 * @return the previous state.
	 */
	public S getPreviousState() {
		return previousState;
	}

	/**
	 * Returns the event that caused the transition.
	 *
	 * @return the event.
	 */
	public E getEvent() {
		return event;
	}

	/**
	 * Returns the new state.
	 *
	 * @return the new state.
	 */
	public S getNewState() {
		return newState;
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("previous", previousState).add("event", event).add("new", newState).toString();
	}
}
