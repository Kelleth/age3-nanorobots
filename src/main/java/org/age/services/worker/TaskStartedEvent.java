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
 * Created: 2014-12-21.
 */

package org.age.services.worker;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.checkerframework.checker.igj.qual.Immutable;

import java.time.LocalDateTime;

/**
 * Event sent when the task has been started in {@link WorkerService}.
 */
@Immutable
public class TaskStartedEvent implements TaskEvent {

	private final LocalDateTime timestamp = LocalDateTime.now();

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
