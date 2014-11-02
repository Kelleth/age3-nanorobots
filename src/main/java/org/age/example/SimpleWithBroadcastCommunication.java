/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.example;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.age.compute.api.BroadcastMessenger;
import org.age.compute.api.MessageListener;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * The simplest possible computation. Completely detached and having no dependencies and no friends.
 */
public class SimpleWithBroadcastCommunication implements Runnable, MessageListener<@NonNull String> {

	private static final Logger log = LoggerFactory.getLogger(SimpleWithBroadcastCommunication.class);

	@Inject	@MonotonicNonNull private BroadcastMessenger messenger;

	@Override
	public void run() {
		log.info("This is the simplest possible example of a computation.");
		log.info("Broadcast messenger: {}.", messenger);

		messenger.registerListener(this);

		for (int i = 0; i < 100; i++) {
			log.info("Iteration {}. Sending message.", i);

			messenger.send("Test message from " + hashCode());

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (final InterruptedException e) {
				log.debug("Interrupted.", e);
				Thread.currentThread().interrupt();
				return;
			}
		}

		messenger.removeListener(this);
	}

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}

	@Override public void onMessage(@NonNull final String message) {
		log.info("Message received: {}.", message);
	}
}
