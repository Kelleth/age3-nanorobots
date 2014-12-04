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
 * Created: 2014-11-07
 */

package org.age.services.worker.internal;

import org.age.services.worker.WorkerMessage;

import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Internal API for worker services.
 */
public interface WorkerCommunication {

	/**
	 * Send a message to other worker services in the distributed environment.
	 *
	 * @param message
	 * 		a message to send.
	 */
	void sendMessage(@NonNull WorkerMessage<Serializable> message);

	ListenableScheduledFuture<?> scheduleAtFixedRate(
			Runnable command, long initialDelay, long period, TimeUnit unit);
}
