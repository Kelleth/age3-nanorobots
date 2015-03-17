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

package org.age.example.mas;

import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.Agent;
import org.age.compute.mas.agent.AgentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public class ReproductionAction implements Action {

	private static final Logger logger = LoggerFactory.getLogger(ReproductionAction.class);

	private Random random = new Random();

	@Override public void execute(Agent<?> parent, Collection<Agent<?>> agents) {
		@SuppressWarnings("unchecked") Collection<Agent<DummyAgent>> castedAgents = (Collection)agents;

		for (Agent<DummyAgent> agent : castedAgents) {
			if (random.nextInt(100) == 0) {
				Agent<DummyAgent> newAgent = AgentBuilder.baseOn(agent).build();
				int energyForNewAgent = agent.behavior().getEnergy() / 2;
				newAgent.behavior().setEnergy(energyForNewAgent);
				agent.behavior().setEnergy(agent.behavior().getEnergy() - energyForNewAgent);

				logger.info("Created new agent from: " + agent.name());
				parent.addChild(newAgent);
			}
		}
	}

}
