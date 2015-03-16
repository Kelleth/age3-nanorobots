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

package org.age.compute.agent.agent;

import org.age.compute.agent.message.Message;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class that describes agent behavior
 */
public abstract class AgentBehavior {

	/**
	 * Step logic
	 */
	public abstract void doStep(int stepNumber);

	protected Map<String, Object> getSettings() {
		// nothing here, this method is enhanced in {@link org.age.compute.agent.agent.EnhancedAgent}
		return Collections.emptyMap();
	}

	protected final void sendMessage(final Message message) {
		// TODO
	}

	public Stream<AgentBehavior> query() {
		// nothing here, this method is enhanced in {@link org.age.compute.agent.agent.Enha ncedAgent}
		return null;
	}

	public <B extends AgentBehavior> Stream<B> query(final Class<B> queryLimiter) {
		return query().filter(queryLimiter::isInstance).map(queryLimiter::cast);
	}

}

