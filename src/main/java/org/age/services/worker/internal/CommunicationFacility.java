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
