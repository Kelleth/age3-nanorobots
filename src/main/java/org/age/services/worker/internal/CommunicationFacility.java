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

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Set;

public interface CommunicationFacility {

	/**
	 * Handles a message.
	 * <p>
	 * Only messages directed to this node will be passed with this method, so implementers do not need to check
	 * whether
	 * they are recipients.
	 *
	 * @param workerMessage
	 * 		a received message (for the current node).
	 * @param <T>
	 * 		a type of the payload.
	 *
	 * @return true if the message should be not processed anymore, false otherwise.
	 */
	<T extends Serializable> boolean onMessage(@NonNull WorkerMessage<T> workerMessage);

	/**
	 * Returns a set of message types that this listener wants to subscribe to.
	 */
	@NonNull @Immutable Set<WorkerMessage.Type> subscribedTypes();
}
