/*
 * Created: 2014-11-07
 * $Id$
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
