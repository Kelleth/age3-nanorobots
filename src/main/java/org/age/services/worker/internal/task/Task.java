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
 * Created: 2015-01-25.
 */

package org.age.services.worker.internal.task;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Wrapper of a single compute task.
 *
 * It is responsible for data consistency of the task.
 */
public interface Task {

	boolean isRunning();

	@NonNull String className();

	@NonNull AbstractApplicationContext springContext();

	/**
	 * @return a future for the running task.
	 *
	 * @throws IllegalStateException
	 * 		when task is not scheduled.
	 */
	@NonNull ListenableScheduledFuture<?> future();

	/**
	 * @return the running task.
	 *
	 * @throws IllegalStateException
	 * 		when task is not scheduled.
	 */
	@NonNull Runnable runnable();

	void pause();

	void resume();

	void stop();

	void cleanUp();
}
