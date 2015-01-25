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

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

@SuppressWarnings("Singleton")
public final class NullTask implements Task {

	public static final NullTask INSTANCE = new NullTask();

	private static final Logger log = LoggerFactory.getLogger(NullTask.class);

	private NullTask() {
		// Empty
	}

	@Override public boolean isRunning() {
		log.warn("Checking 'running' status of the NULL task.");
		return false;
	}

	@Override public @NonNull String className() {
		throw new UnsupportedOperationException("NULL task does not return values.");
	}

	@Override public @NonNull AbstractApplicationContext springContext() {
		throw new UnsupportedOperationException("NULL task does not return values.");
	}

	@Override public @NonNull ListenableScheduledFuture<?> future() {
		throw new UnsupportedOperationException("NULL task does not return values.");
	}

	@Override public @NonNull Runnable runnable() {
		throw new UnsupportedOperationException("NULL task does not return values.");
	}

	@Override public void pause() {
		log.warn("Pausing up NULL task.");
	}

	@Override public void resume() {
		log.warn("Resuming NULL task.");
	}

	@Override public void stop() {
		log.warn("Stopping NULL task.");
	}

	@Override public void cleanUp() {
		log.warn("Cleaning up NULL task.");
	}

	@Override public boolean equals(final Object obj) {
		return obj instanceof NullTask;
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
