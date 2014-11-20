package org.age.util.fsm;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A descriptor of the transition in the transition table.
 * <p>
 * Package-protected class.
 *
 * @param <S>
 * 		a states type.
 * @param <E>
 * 		an events type
 */
@SuppressWarnings("unchecked")
final class TransitionDescriptor<S extends Enum<S>, E extends Enum<E>> {

	private static final Consumer<FSM<?, ?>> ILLEGAL_ACTION = fsm -> {
		throw new IllegalTransitionException("Transition is illegal.");
	};

	public static final TransitionDescriptor<?, ?> NULL = new TransitionDescriptor(null, null, Collections.emptySet(),
	                                                                               ILLEGAL_ACTION);

	public static final Consumer<?> EMPTY_ACTION = fsm -> {};

	@Nullable private final S initial;

	@Nullable private final E event;

	@NonNull private final Consumer<FSM<S, E>> action;

	@NonNull private final Set<S> target;

	TransitionDescriptor(@Nullable final S initial, @Nullable final E event, @NonNull final Collection<S> target,
	                     @Nullable final Consumer<FSM<S, E>> action) {
		this.initial = initial;
		this.event = event;
		this.action = isNull(action) ? (Consumer<FSM<S, E>>)EMPTY_ACTION : action;
		this.target = ImmutableSet.copyOf(requireNonNull(target));
	}

	@NonNull static <V extends Enum<V>, Z extends Enum<Z>> TransitionDescriptor<V, Z> nullDescriptor() {
		return (TransitionDescriptor<V, Z>)NULL;
	}

	@NonNull Consumer<FSM<S, E>> action() {
		return action;
	}

	@NonNull Set<S> target() {
		return target;
	}

	@Nullable S initial() {
		return initial;
	}

	@Nullable E event() {
		return event;
	}

	@Override public String toString() {
		return format("(%s : %s : %s)", initial, event, target);
	}
}
