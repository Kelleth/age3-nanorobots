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
 * Created: 2014-12-20.
 */

package org.age.services.worker.internal.task;

import org.age.compute.api.Pauseable;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Wrapper of a single compute task that can be paused.
 *
 * It is responsible for data consistency of the task.
 */
@ThreadSafe
final class PauseableStartedTask extends StartedTask {

	private static final Logger log = LoggerFactory.getLogger(PauseableStartedTask.class);

	private final AtomicBoolean paused = new AtomicBoolean(false);

	PauseableStartedTask(final @NonNull String className, final @NonNull AbstractApplicationContext springContext,
	                     final @NonNull Pauseable runnable, final @NonNull ListenableScheduledFuture<?> future) {
		super(className, springContext, runnable, future);
	}

	@Override public void pause() {
		if (paused.get()) {
			log.debug("The task has been already paused.");
			return;
		}
		if (!isRunning()) {
			log.warn("Cannot pause not running task.");
			return;
		}

		log.debug("Pausing the task {}.", runnable);
		((Pauseable)runnable).pause();
		paused.set(true);
	}

	@Override public void resume() {
		if (!paused.get()) {
			log.debug("The task has not been paused.");
			return;
		}
		if (!isRunning()) {
			log.warn("Cannot resume finished task.");
			return;
		}

		log.debug("Resuming the task {}.", runnable);
		((Pauseable)runnable).resume();
		paused.set(false);
	}

}
