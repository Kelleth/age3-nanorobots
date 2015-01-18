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
 * Created: 2014-12-28.
 */

package org.age.services.worker;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import org.age.services.ServiceFailureEvent;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Event sent when the task running in {@link WorkerService} has failed due to an exception.
 */
@Immutable
public final class TaskFailedEvent implements WorkerServiceEvent, ServiceFailureEvent {

	private final Throwable cause;

	public TaskFailedEvent(final @NonNull Throwable cause) {
		this.cause = requireNonNull(cause);
	}

	@Override public @NonNull String serviceName() {
		return WorkerService.class.getSimpleName();
	}

	@Override public @NonNull Throwable cause() {
		return cause;
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(cause).toString();
	}
}
