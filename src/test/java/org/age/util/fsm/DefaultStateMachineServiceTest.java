package org.age.util.fsm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

import com.google.common.eventbus.EventBus;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;

public final class DefaultStateMachineServiceTest {
	private enum State {
		STATE1,
		STATE2,
		STATE3,
		ERROR,
		END
	}

	private enum Event {
		EVENT1,
		EVENT2,
		EVENT3,
		FAIL
	}

	private static final String SERVICE_NAME = "name";

	private static final Consumer<FSM<State, Event>> consumer1 = fsm -> {};

	private static final Consumer<FSM<State, Event>> consumer2 = fsm -> {};

	private final List<Throwable> collectedThrowables = newArrayList();

	private final Consumer<Throwable> exceptionHandler = collectedThrowables::add;

	@Mock private EventBus eventBus;

	private StateMachineServiceBuilder<State, Event> builder;

	private DefaultStateMachineService<State, Event> fsmService;

	@SuppressWarnings({"ProhibitedExceptionThrown", "CastToConcreteClass"}) @BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		collectedThrowables.clear();
		builder = StateMachineServiceBuilder.withStatesAndEvents(State.class, Event.class);
		builder.withName(SERVICE_NAME)
				.startWith(State.STATE1)
				.withEventBus(eventBus)
				.terminateIn(State.END, State.ERROR)
				.ifFailed().fireAndCall(Event.FAIL, exceptionHandler)
				.inAnyState().on(Event.FAIL).goTo(State.ERROR).commit()
				.in(State.STATE1).on(Event.EVENT1).goTo(State.STATE2).commit()
				.in(State.STATE2).on(Event.EVENT2).execute((fsm) -> {}).goTo(State.STATE3).commit()
				.in(State.STATE1).on(Event.EVENT2).goTo(State.STATE2).execute(fsm -> {
					throw new RuntimeException("FAILED");
				}).commit()
				.synchronous();
		fsmService = (DefaultStateMachineService<State, Event>)builder.build();
	}

	@Test public void testInitialState() {
		//assertThat(fsmService.isRunning()).isTrue();
		assertThat(fsmService.isInState(State.STATE1)).isTrue();
	}

	@Test public void testSingleTransition_correct() {
		fsmService.fire(Event.EVENT1);
		fsmService.execute();

		assertThat(fsmService.isInState(State.STATE2)).isTrue();
	}

	@Test public void testFailure() {
		fsmService.fire(Event.FAIL);
		fsmService.execute();

		assertThat(fsmService.isInState(State.ERROR)).isTrue();
	}

	@Test public void testSingleTransition_failedAction() {
		fsmService.fire(Event.EVENT2);
		fsmService.execute();

		assertThat(fsmService.isInState(State.STATE2)).isFalse();

		assertThat(collectedThrowables).isNotNull().hasSize(1);
		assertThat(collectedThrowables.get(0)).isInstanceOf(RuntimeException.class);
	}

	@Test public void testMultipleTransitions() {
		fsmService.fire(Event.EVENT1);
		fsmService.execute();
		fsmService.fire(Event.EVENT2);
		fsmService.execute();

		assertThat(fsmService.isInState(State.STATE3)).isTrue();

		assertThat(collectedThrowables).isNotNull().hasSize(0);
	}
}