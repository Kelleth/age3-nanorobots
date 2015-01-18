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
 * Created: 2014-10-07.
 */

package org.age.compute.api;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Messenger for the broadcast (pub-sub type) communication between workers.
 *
 * <p>Implementations need to be thread-safe, as they are presented to possibly multi-threaded compute code.
 */
@ThreadSafe
public interface BroadcastMessenger {

	/**
	 * Sends the message to all neighbouring workers.
	 *
	 * @param message a message to send.
	 * @param <T> a type of the payload.
	 */
	<T extends Serializable> void send(@NonNull T message);

	/**
	 * Registers a listener that will receive all incoming messages.
	 *
	 * @param listener a listener to register.
	 * @param <T> a type of the payload.
	 */
	<T extends Serializable> void registerListener(@NonNull MessageListener<T> listener);

	/**
	 * Removes the listener.
	 *
	 * @param listener a listener to remove.
	 * @param <T> a type of the payload.
	 */
	<T extends Serializable> void removeListener(@NonNull MessageListener<T> listener);
}
