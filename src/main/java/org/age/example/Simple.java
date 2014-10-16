/*
 * Created: 2014-10-07
 * $Id$
 */

package org.age.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * The simplest possible computation. Completely detached and having no dependencies and no friends.
 */
public class Simple implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(Simple.class);

	@Override
	public void run() {
		log.info("This is the simplest possible example of a computation.");
	}

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
