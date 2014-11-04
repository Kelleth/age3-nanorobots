package org.age.util.fsm;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.fail;

import com.google.common.collect.Table;

public class StateMachineServiceBuilderTest {

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

	private final Consumer<FSM<State, Event>> consumer1 = fsm -> {};
	private final Consumer<FSM<State, Event>> consumer2 = fsm -> {};

	private static class StateChangedEvent_Helper extends StateChangedEvent<State, Event> {
		protected StateChangedEvent_Helper(final State previousState, final Event event, final State newState) {
			super(previousState, event, newState);
		}
	}

	@BeforeMethod
	public void setUp() {
		builder = StateMachineServiceBuilder.withStatesAndEvents(State.class, Event.class);
	}

	@AfterMethod
	public void tearDown() throws Exception {

	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testConstructor_nullEventsAndStates() {
		StateMachineServiceBuilder.<State, Event>withStatesAndEvents(null, null);
		fail("Null parameters were accepted.");
	}

	@Test
	public void testConstructor_correctInitialization() {
		assertThat(builder.getStateClass(), is(equalTo(State.class)));
		assertThat(builder.getEventClass(), is(equalTo(Event.class)));
		assertThat(builder.getStateChangedEventClass(), is(equalTo(StateChangedEvent.class)));
	}

	@Test
	public void testNotifyWithType() {
		builder.notifyWithType(StateChangedEvent_Helper.class);
		assertThat(builder.getStateChangedEventClass(), is(equalTo(StateChangedEvent_Helper.class)));
	}

	@Test
	public void testWithName() throws Exception {
		builder.withName(SERVICE_NAME);
		assertThat(builder.getName(), is(equalTo(SERVICE_NAME)));
	}

	@Test
	public void testStateDefinition_singleEvent() {
		builder.in(State.STATE1).on(Event.EVENT1).execute(consumer1).goTo(State.STATE2).commit();

		final Table<State, Event, Consumer<FSM<State, Event>>> actions = builder.getActions();
		final Table<State, Event, Set<State>> transitions = builder.getTransitions();

		final Consumer<FSM<State, Event>> action = actions.get(State.STATE1, Event.EVENT1);
		final Set<State> states = transitions.get(State.STATE1, Event.EVENT1);

		assertThat(action, is(notNullValue()));
		assertThat(action, is(equalTo(consumer1)));
		assertThat(states, is(not(empty())));
		assertThat(states, contains(State.STATE2));
	}

	@Test
	public void testStateDefinition_multipleEvents() {
		builder.in(State.STATE1)
		       .on(Event.EVENT1)
		       .execute(consumer1)
		       .goTo(State.STATE2)
		       .on(Event.EVENT2)
		       .execute(consumer2)
		       .goTo(State.STATE3)
		       .commit();

		final Table<State, Event, Consumer<FSM<State, Event>>> actions = builder.getActions();
		final Table<State, Event, Set<State>> transitions = builder.getTransitions();

		final Consumer<FSM<State, Event>> action1 = actions.get(State.STATE1, Event.EVENT1);
		final Set<State> states1 = transitions.get(State.STATE1, Event.EVENT1);
		final Consumer<FSM<State, Event>> action2 = actions.get(State.STATE1, Event.EVENT2);
		final Set<State> states2 = transitions.get(State.STATE1, Event.EVENT2);

		assertThat(action1, is(notNullValue()));
		assertThat(action1, is(equalTo(consumer1)));
		assertThat(states1, is(not(empty())));
		assertThat(states1, contains(State.STATE2));

		assertThat(action2, is(notNullValue()));
		assertThat(action2, is(equalTo(consumer2)));
		assertThat(states2, is(not(empty())));
		assertThat(states2, contains(State.STATE3));
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
	}


	@Test
	public void testAnyStateDefinition_singleEvent() {
		builder.inAnyState().on(Event.EVENT1).execute(consumer1).goTo(State.STATE2).commit();

		final Map<Event, Consumer<FSM<State, Event>>> actions = builder.getAnyActions();
		final Map<Event, Set<State>> transitions = builder.getAnyTransitions();

		final Consumer<FSM<State, Event>> action = actions.get(Event.EVENT1);
		final Set<State> states = transitions.get(Event.EVENT1);

		assertThat(action, is(notNullValue()));
		assertThat(action, is(equalTo(consumer1)));
		assertThat(states, is(not(empty())));
		assertThat(states, contains(State.STATE2));
	}

	@Test
	public void testAnyStateDefinition_multipleEvents() {
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

		assertThat(action1, is(notNullValue()));
		assertThat(action1, is(equalTo(consumer1)));
		assertThat(states1, is(not(empty())));
		assertThat(states1, contains(State.STATE2));

		assertThat(action2, is(notNullValue()));
		assertThat(action2, is(equalTo(consumer2)));
		assertThat(states2, is(not(empty())));
		assertThat(states2, contains(State.STATE3));
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
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testAnyStateDefinition_noTransitionProvided() {
		builder.inAnyState().on(Event.EVENT1).commit();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testStartWith() {
		builder.startWith(State.STATE1);
		assertThat(builder.getInitialState(), is(equalTo(State.STATE1)));

		builder.startWith(null);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testTerminateIn() {
		builder.terminateIn(State.STATE1);

		assertThat(builder.getTerminalStates(), is(iterableWithSize(1)));
		assertThat(builder.getTerminalStates(), contains(State.STATE1));

		builder.terminateIn(State.STATE1, State.STATE2);

		assertThat(builder.getTerminalStates(), is(iterableWithSize(2)));
		assertThat(builder.getTerminalStates(), contains(State.STATE1, State.STATE2));

		builder.terminateIn();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testIfFailed() {
		builder.ifFailed().fireAndCall(Event.EVENT1, throwables -> {});

		assertThat(builder.getFailureEvent(), is(equalTo(Event.EVENT1)));

		builder.ifFailed().fireAndCall(null, throwables -> {});
	}

	@Test
	public void testMinimalBuild() {
		final StateMachineService<State, Event> service = builder.withName(SERVICE_NAME)
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
