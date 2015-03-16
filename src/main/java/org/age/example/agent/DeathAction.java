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

package org.age.example.agent;

import org.age.compute.agent.agent.Agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeathAction extends org.age.compute.agent.action.DeathAction {

	private static final Logger logger = LoggerFactory.getLogger(DeathAction.class);

	@Override @SuppressWarnings("unchecked") protected boolean shouldDie(Agent<?> agent) {
		return DummyAgent.class.isAssignableFrom(agent.behaviorClass()) && checkEnergy((Agent<DummyAgent>)agent);
	}

	private boolean checkEnergy(Agent<DummyAgent> agent) {
		if (agent.behavior().getEnergy() == 0) {
			logger.debug("Agent: " + agent.name() + " is about to die");
			return true;
		}
		return false;
	}

}
