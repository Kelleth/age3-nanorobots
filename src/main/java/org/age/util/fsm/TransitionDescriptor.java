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
 * $Id: e51f903c5f405a8935aa42b043e7def1caacb619 $
 */

package org.age.util.fsm;

import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;

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
 *
 * @author AGH AgE Team
 */
class TransitionDescriptor<S extends Enum<S>, E extends Enum<E>> {

	@SuppressWarnings("unchecked") public static final TransitionDescriptor<?, ?> NULL = new TransitionDescriptor(null,
	                                                                                                              null,
	                                                                                                              null,
	                                                                                                              null) {
		@Override
		boolean isNull() {
			return true;
		}

		@Override
		public String toString() {
			return format("(null)");
		}
	};
	private final Consumer<FSM<S, E>> EMPTY_ACTION = fsm -> {
	};
	private final S initial;
	private final E event;
	@NonNull private final Consumer<FSM<S, E>> action;
	private final Set<S> target;

	public TransitionDescriptor(@Nullable final S initial, @Nullable final E event, @Nullable final Set<S> target,
	                            @Nullable final Consumer<FSM<S, E>> action) {
		this.initial = initial;
		this.event = event;
		this.action = action != null ? action : EMPTY_ACTION;
		this.target = target;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	public static <V extends Enum<V>, Z extends Enum<Z>> TransitionDescriptor<V, Z> getNull() {
		return (TransitionDescriptor<V, Z>)NULL;
	}

	@NonNull
	final Consumer<FSM<S, E>> getAction() {
		return action;
	}

	@Nullable
	final Set<S> getTarget() {
		return target;
	}

	@Nullable
	final S getInitial() {
		return initial;
	}

	@Nullable
	final E getEvent() {
		return event;
	}

	@SuppressWarnings("static-method")
	boolean isNull() {
		return false;
	}

	@Override
	public String toString() {
		return format("(%s : %s : %s)", initial, event, target);
	}
}
