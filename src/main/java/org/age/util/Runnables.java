package org.age.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Runnables {

	private static final Logger log = LoggerFactory.getLogger(Runnables.class);

	private Runnables() {}

	public static Runnable swallowingRunnable(final Runnable runnable) {
		return () -> {
			try {
				runnable.run();
			} catch (final Throwable t) {
				log.error("Runnable threw an error.", t);
			}
		};
	}
}
