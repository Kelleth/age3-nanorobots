/*
 * Created: 2014-10-07
 * $Id$
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
