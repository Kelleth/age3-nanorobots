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
 * Created: 2014-10-07
 */

package org.age.example;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
