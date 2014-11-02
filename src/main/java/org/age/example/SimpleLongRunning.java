/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.example;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * The simplest possible computation. Completely detached and having no dependencies and no friends. Long running
 * version.
 */
public class SimpleLongRunning implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SimpleLongRunning.class);

	@Override
	public void run() {
		log.info("This is the simplest possible example of a computation.");
		for (int i = 0; i < 100; i++) {
			log.info("Iteration {}.", i);

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (final InterruptedException e) {
				log.debug("Interrupted.", e);
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
