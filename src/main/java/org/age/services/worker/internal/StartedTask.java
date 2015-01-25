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

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.nonNull;

import org.age.compute.api.Pauseable;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Wrapper of a single compute task.
 *
 * It is responsible for data consistency of the task.
 */
@ThreadSafe
final class StartedTask implements Task {

	private static final Logger log = LoggerFactory.getLogger(StartedTask.class);

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final String className;

	private final AbstractApplicationContext springContext;

	private final AtomicBoolean paused = new AtomicBoolean(false);

	@GuardedBy("lock") private final Runnable runnable;

	@GuardedBy("lock") private final ListenableScheduledFuture<?> future;

	StartedTask(final @NonNull String className, final @NonNull AbstractApplicationContext springContext,
	            final @NonNull Runnable runnable, final @NonNull ListenableScheduledFuture<?> future) {
		assert nonNull(className) && nonNull(springContext) && nonNull(runnable) && nonNull(future);

		this.className = className;
		this.springContext = springContext;
		this.runnable = runnable;
		this.future = future;
	}

	@Override public boolean isRunning() {
		lock.readLock().lock();
		try {
			return !future.isDone();
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override @NonNull public String className() {
		return className;
	}

	@Override @NonNull public AbstractApplicationContext springContext() {
		return springContext;
	}

	/**
	 * @return a future for the running task.
	 *
	 * @throws IllegalStateException
	 * 		when task is not scheduled.
	 */
	@Override @NonNull public ListenableScheduledFuture<?> future() {
		lock.readLock().lock();
		try {
			return future;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @return the running task.
	 *
	 * @throws IllegalStateException
	 * 		when task is not scheduled.
	 */
	@Override @NonNull public Runnable runnable() {
		lock.readLock().lock();
		try {
			return runnable;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override public void pause() {
		if (!(runnable instanceof Pauseable)) {
			log.debug("The task is not pauseable.");
			return;
		}
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
		if (!(runnable instanceof Pauseable)) {
			log.debug("The task is not pauseable.");
			return;
		}
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

	@Override public void stop() {
		if (!isRunning()) {
			log.warn("Task is already stopped.");
			return;
		}

		log.debug("Stopping task {}.", runnable);
		lock.writeLock().lock();
		try {
			final boolean canceled = future.cancel(true);
			if (!canceled) {
				log.warn("Could not cancel the task. Maybe it already stopped?");
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override public void cleanUp() {
		log.debug("Cleaning up after task.");
		springContext.destroy();
	}

	@Override public String toString() {
		lock.readLock().lock();
		try {
			return toStringHelper(this).add("classname", className).add("runnable", runnable).toString();
		} finally {
			lock.readLock().unlock();
		}
	}

}
