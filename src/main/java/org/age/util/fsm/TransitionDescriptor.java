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

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

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
