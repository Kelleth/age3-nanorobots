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

import org.age.compute.agent.action.Action;
import org.age.compute.agent.agent.Agent;

import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public class EnergyTransferAction implements Action {

	private static final Logger logger = LoggerFactory.getLogger(EnergyTransferAction.class);

	@Override public void execute(Agent<?> parent, Collection<Agent<?>> agents) {
		@SuppressWarnings("unchecked") Collection<Agent<DummyAgent>> castedAgents = (Collection)agents;

		for (Agent<DummyAgent> agent : castedAgents) {
			Agent<DummyAgent> randomAgent = pickDifferentThan(castedAgents, agent);
			if (new Random().nextInt(2) == 0) {
				executeForSingleAgent(agent, randomAgent);
			}
		}
	}

	private <E> E pickDifferentThan(Collection<E> elements, E element) {
		E randomAgent;
		do {
			randomAgent = pick(elements);
		} while (randomAgent == element);
		return randomAgent;
	}

	private <E> E pick(Collection<E> elements) {
		return Iterables.get(elements, new Random().nextInt(elements.size()));
	}

	private void executeForSingleAgent(Agent<DummyAgent> agent, Agent<DummyAgent> otherAgent) {
		if (agent.behavior().getEnergy() > otherAgent.behavior().getEnergy()) {
			logger.info("Agent: " + agent.name() + " have more energy than: " + otherAgent.name());

			int otherAgentEnergy = otherAgent.behavior().getEnergy();
			int amountOfEnergyToTransfer = new Random().nextInt((int)(0.8 * otherAgentEnergy) + 1);

			agent.behavior().setEnergy(agent.behavior().getEnergy() + amountOfEnergyToTransfer);
			otherAgent.behavior().setEnergy(otherAgent.behavior().getEnergy() - amountOfEnergyToTransfer);

			logger.info("Agent: " + agent.name() + " have now: " + agent.behavior().getEnergy() + " energy points");
			logger.info("Agent: " + otherAgent.name() + " have now: " + otherAgent.behavior().getEnergy()
			            + " energy points");
		}
	}

}
