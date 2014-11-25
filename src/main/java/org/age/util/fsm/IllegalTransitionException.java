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

public final class IllegalTransitionException extends RuntimeException {

	private static final long serialVersionUID = -8827262839354852835L;

	public IllegalTransitionException(final String message) {
		super(message);
	}

	public IllegalTransitionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public IllegalTransitionException(final Throwable cause) {
		super(cause);
	}
}
