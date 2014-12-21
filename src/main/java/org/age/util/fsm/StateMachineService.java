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

import org.checkerframework.checker.nullness.qual.NonNull;

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
 * @see StateMachineServiceBuilder
 */
public interface StateMachineService<S extends Enum<S>, E extends Enum<E>> extends FSM<S, E> {

	/**
	 * Fires an event.
	 *
	 * @param event an event to fire.
	 */
	void fire(@NonNull E event);

	/**
	 * Returns whether the service is isRunning.
	 */
	boolean isRunning();

	/**
	 * Checks atomically whether the service is in the given state.
	 *
	 * @param state
	 * 		a state to check.
	 *
	 * @return true if the service is in the given state, false otherwise.
	 */
	boolean isInState(@NonNull S state);

	/**
	 * Returns the current state of the machine.
	 */
	@NonNull S currentState();

	/**
	 * Tells whether the machine has failed.
	 */
	boolean isFailed();

	/**
	 * Tells whether the machine is already terminated.
	 */
	boolean isTerminated();

	void awaitTermination() throws InterruptedException;

	/**
	 * Shutdowns and cleans up the service.
	 *
	 * @throws IllegalStateException
	 * 		if service has not terminated yet.
	 *
	 * @see #isTerminated()
	 */
	void shutdown();

	/**
	 * Shutdowns and cleans up the service even if it have not terminate yet.
	 *
	 * @see #isTerminated()
	 * @see #shutdown()
	 */
	void forceShutdown();

}
