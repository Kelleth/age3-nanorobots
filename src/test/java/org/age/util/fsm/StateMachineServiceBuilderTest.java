package org.age.util.fsm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import com.google.common.collect.Table;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class StateMachineServiceBuilderTest {

	private StateMachineServiceBuilder<State, Event> builder;

	private enum State {
		STATE1,
		STATE2,
		STATE3
	}

	private enum Event {
		EVENT1,
		EVENT2,
		EVENT3
	}

	private static final String SERVICE_NAME = "name";

	private static final Consumer<FSM<State, Event>> consumer1 = fsm -> {};

	private static final Consumer<FSM<State, Event>> consumer2 = fsm -> {};

	private static class StateChangedEvent_Helper extends StateChangedEvent<State, Event> {
		protected StateChangedEvent_Helper(final State previousState, final Event event, final State newState) {
			super(previousState, event, newState);
		}
	}

	@BeforeMethod public void setUp() {
		builder = StateMachineServiceBuilder.withStatesAndEvents(State.class, Event.class);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testConstructor_nullEventsAndStates() {
		StateMachineServiceBuilder.<State, Event>withStatesAndEvents(null, null);

		failBecauseExceptionWasNotThrown(NullPointerException.class);
	}

	@Test public void testConstructor_correctInitialization() {
		assertThat(builder.stateClass()).isEqualTo(State.class);
		assertThat(builder.eventClass()).isEqualTo(Event.class);
		assertThat(builder.getStateChangedEventClass()).isEqualTo(StateChangedEvent.class);
	}

	@Test public void testNotifyWithType() {
		builder.notifyWithType(StateChangedEvent_Helper.class);
		assertThat(builder.getStateChangedEventClass()).isEqualTo(StateChangedEvent_Helper.class);
	}

	@Test public void testWithName() throws Exception {
		builder.withName(SERVICE_NAME);
		assertThat(builder.name()).isEqualTo(SERVICE_NAME);
	}

	@Test public void testStateDefinition_singleEvent() {
		builder.in(State.STATE1).on(Event.EVENT1).execute(consumer1).goTo(State.STATE2).commit();

		final Table<State, Event, Consumer<FSM<State, Event>>> actions = builder.actions();
		final Table<State, Event, Set<State>> transitions = builder.transitions();

		final Consumer<FSM<State, Event>> action = actions.get(State.STATE1, Event.EVENT1);
		final Set<State> states = transitions.get(State.STATE1, Event.EVENT1);

		assertThat(action).isNotNull();
		assertThat(action).isEqualTo(consumer1);
		assertThat(states).isNotEmpty();
		assertThat(states).contains(State.STATE2);
	}

	@Test public void testStateDefinition_multipleEvents() {
		builder.in(State.STATE1)
		       .on(Event.EVENT1)
		       .execute(consumer1)
		       .goTo(State.STATE2)
		       .on(Event.EVENT2)
		       .execute(consumer2)
		       .goTo(State.STATE3)
		       .commit();

		final Table<State, Event, Consumer<FSM<State, Event>>> actions = builder.actions();
		final Table<State, Event, Set<State>> transitions = builder.transitions();

		final Consumer<FSM<State, Event>> action1 = actions.get(State.STATE1, Event.EVENT1);
		final Set<State> states1 = transitions.get(State.STATE1, Event.EVENT1);
		final Consumer<FSM<State, Event>> action2 = actions.get(State.STATE1, Event.EVENT2);
		final Set<State> states2 = transitions.get(State.STATE1, Event.EVENT2);

		assertThat(action1).isNotNull();
		assertThat(action1).isEqualTo(consumer1);
		assertThat(states1).isNotEmpty();
		assertThat(states1).contains(State.STATE2);

		assertThat(action2).isNotNull();
		assertThat(action2).isEqualTo(consumer2);
		assertThat(states2).isNotEmpty();
		assertThat(states2).contains(State.STATE3);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testStateDefinition_incorrectDefinition() {
		builder.in(State.STATE1)
		       .on(Event.EVENT1)
		       .execute(consumer1)
		       .on(Event.EVENT2)
		       .execute(consumer2)
		       .goTo(State.STATE3)
		       .commit();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testStateDefinition_noTransitionProvided() {
		builder.in(State.STATE1).on(Event.EVENT1).commit();

		failBecauseExceptionWasNotThrown(IllegalStateException.class);
	}


	@Test public void testAnyStateDefinition_singleEvent() {
		builder.inAnyState().on(Event.EVENT1).execute(consumer1).goTo(State.STATE2).commit();

		final Map<Event, Consumer<FSM<State, Event>>> actions = builder.getAnyActions();
		final Map<Event, Set<State>> transitions = builder.getAnyTransitions();

		final Consumer<FSM<State, Event>> action = actions.get(Event.EVENT1);
		final Set<State> states = transitions.get(Event.EVENT1);

		assertThat(action).isNotNull();
		assertThat(action).isEqualTo(consumer1);
		assertThat(states).isNotEmpty();
		assertThat(states).contains(State.STATE2);
	}

	@Test public void testAnyStateDefinition_multipleEvents() {
		builder.inAnyState()
		       .on(Event.EVENT1)
		       .execute(consumer1)
		       .goTo(State.STATE2)
		       .on(Event.EVENT2)
		       .execute(consumer2)
		       .goTo(State.STATE3)
		       .commit();

		final Map<Event, Consumer<FSM<State, Event>>> actions = builder.getAnyActions();
		final Map<Event, Set<State>> transitions = builder.getAnyTransitions();

		final Consumer<FSM<State, Event>> action1 = actions.get(Event.EVENT1);
		final Set<State> states1 = transitions.get(Event.EVENT1);
		final Consumer<FSM<State, Event>> action2 = actions.get(Event.EVENT2);
		final Set<State> states2 = transitions.get(Event.EVENT2);

		assertThat(action1).isNotNull();
		assertThat(action1).isEqualTo(consumer1);
		assertThat(states1).isNotEmpty();
		assertThat(states1).contains(State.STATE2);

		assertThat(action2).isNotNull();
		assertThat(action2).isEqualTo(consumer2);
		assertThat(states2).isNotEmpty();
		assertThat(states2).contains(State.STATE3);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAnyStateDefinition_incorrectDefinition() {
		builder.inAnyState()
		       .on(Event.EVENT1)
		       .execute(consumer1)
		       .on(Event.EVENT2)
		       .execute(consumer2)
		       .goTo(State.STATE3)
		       .commit();

		failBecauseExceptionWasNotThrown(IllegalStateException.class);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAnyStateDefinition_noTransitionProvided() {
		builder.inAnyState().on(Event.EVENT1).commit();

		failBecauseExceptionWasNotThrown(IllegalStateException.class);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testStartWith() {
		builder.startWith(State.STATE1);
		assertThat(builder.initialState()).isEqualTo(State.STATE1);

		builder.startWith(null);

		failBecauseExceptionWasNotThrown(NullPointerException.class);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTerminateIn() {
		builder.terminateIn(State.STATE1);

		assertThat(builder.terminalStates()).hasSize(1);
		assertThat(builder.terminalStates()).contains(State.STATE1);

		builder.terminateIn(State.STATE1, State.STATE2);

		assertThat(builder.terminalStates()).hasSize(2);
		assertThat(builder.terminalStates()).contains(State.STATE1, State.STATE2);

		builder.terminateIn();

		failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
	}

	@Test(expectedExceptions = NullPointerException.class) public void testIfFailed() {
		builder.ifFailed().fireAndCall(Event.EVENT1, throwables -> {});

		assertThat(builder.getFailureEvent()).isEqualTo(Event.EVENT1);

		builder.ifFailed().fireAndCall(null, throwables -> {});

		failBecauseExceptionWasNotThrown(NullPointerException.class);
	}

	@Test public void testMinimalBuild() {
		builder.withName(SERVICE_NAME)
		       .startWith(State.STATE1)
		       .terminateIn(State.STATE3)
		       .ifFailed()
		       .fireAndCall(Event.EVENT2, throwables -> {})
		       .in(State.STATE1)
		       .on(Event.EVENT1)
		       .goTo(State.STATE2)
		       .commit()
		       .build();

	}

}
