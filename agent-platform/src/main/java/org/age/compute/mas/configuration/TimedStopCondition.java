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

package org.age.compute.mas.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimedStopCondition implements StopCondition {

	private static final Logger log = LoggerFactory.getLogger(TimedStopCondition.class);

	private final Duration desiredDuration;

	private final LocalDateTime startedAt = LocalDateTime.now();

	public TimedStopCondition(final Duration desiredDuration) {
		this.desiredDuration = desiredDuration;
	}

	@Override public boolean isReached() {
		if (Duration.between(startedAt, LocalDateTime.now()).toMillis() >= desiredDuration.toMillis()) {
			log.info("Time's up");
			return true;
		}
		return false;
	}
}
