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

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.nonNull;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Wrapper of a single compute task.
 *
 * It is responsible for data consistency of the task.
 */
@ThreadSafe
final class StartedTask {

	private static final Logger log = LoggerFactory.getLogger(StartedTask.class);

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final String className;

	private final AbstractApplicationContext springContext;

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

	boolean isRunning() {
		lock.readLock().lock();
		try {
			return !future.isDone();
		} finally {
			lock.readLock().unlock();
		}
	}

	@NonNull String className() {
		return className;
	}

	@NonNull AbstractApplicationContext springContext() {
		return springContext;
	}

	/**
	 * @return a future for the running task.
	 *
	 * @throws IllegalStateException
	 * 		when task is not scheduled.
	 */
	@NonNull ListenableScheduledFuture<?> future() {
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
	@NonNull Runnable runnable() {
		lock.readLock().lock();
		try {
			return runnable;
		} finally {
			lock.readLock().unlock();
		}
	}

	void stop() {
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

	void cleanUp() {
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
