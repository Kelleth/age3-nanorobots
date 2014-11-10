/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.compute.api;

import org.checkerframework.checker.igj.qual.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Messenger for the unicast communication between workers.
 *
 * <p>Implementations need to be thread-safe, as they are presented to possibly multi-threaded compute code.
 */
@ThreadSafe
public interface UnicastMessenger {

	@Immutable @NonNull WorkerAddress address();

	@Immutable @NonNull Set<WorkerAddress> neighbours();

	/**
	 * Sends the message to the specified worker.
	 *
	 * @param receiver a recipient of the message.
	 * @param message a message to send.
	 * @param <T> a type of the payload.
	 */
	<T extends Serializable> void send(@NonNull WorkerAddress receiver, @NonNull T message);

	/**
	 * Sends the message to the specified workers.
	 *
	 * @param receivers a set of recipients of the message.
	 * @param message a message to send.
	 * @param <T> a type of the payload.
	 */
	<T extends Serializable> void send(@NonNull Set<WorkerAddress> receivers, @NonNull T message);

	/**
	 * Registers a listener that will receive all incoming messages target for this address (obtained via {@link #initializeBox()}).
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
