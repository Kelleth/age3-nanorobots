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

/*
 * Created: 20.12.14.
 */

package org.age.services.worker;

public class FailedComputationSetupException extends RuntimeException {

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.  <p>Note that the detail message associated with
	 * {@code cause} is <i>not</i> automatically incorporated in
	 * this runtime exception's detail message.
	 *
	 * @param message
	 * 		the detail message (which is saved for later retrieval
	 * 		by the {@link #getMessage()} method).
	 * @param cause
	 * 		the cause (which is saved for later retrieval by the
	 * 		{@link #getCause()} method).  (A <tt>null</tt> value is
	 * 		permitted, and indicates that the cause is nonexistent or
	 * 		unknown.)
	 *
	 * @since 1.4
	 */
	public FailedComputationSetupException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
