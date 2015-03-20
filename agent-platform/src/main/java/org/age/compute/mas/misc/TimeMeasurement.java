/*
 * Copyright (C) 2014-2015 Intelligent Information Systems Group.
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

package org.age.compute.mas.misc;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class TimeMeasurement {

	private static final Logger log = LoggerFactory.getLogger(TimeMeasurement.class);

	private TimeMeasurement() {}

	public static <T> T measureTime(final Supplier<T> func, final String messageToLog) {
		requireNonNull(func);
		requireNonNull(messageToLog);

		final Stopwatch stopwatch = Stopwatch.createStarted();
		try {
			return func.get();
		} finally {
			log.debug(String.format("%s %.2fs", messageToLog, (double)stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000));
		}
	}
}
