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
