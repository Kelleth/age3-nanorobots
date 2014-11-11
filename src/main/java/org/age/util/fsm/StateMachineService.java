package org.age.util.fsm;

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
	void fire(E event);

	/**
	 * Returns whether the service is isRunning.
	 */
	boolean isRunning();

	/**
	 * Tells whether the machine is in a given state.
	 *
	 * @param state a state to check.
	 */
	boolean isInState(S state);

	/**
	 * Tells whether the machine is already isTerminated.
	 **/
	boolean isTerminated();

	boolean isTerminating();

	void shutdown();

	/**
	 * Returns the current state of the machine.
	 */
	S getCurrentState();
}
