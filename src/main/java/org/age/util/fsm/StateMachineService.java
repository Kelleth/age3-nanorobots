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
	 **/
	boolean isTerminated();

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
