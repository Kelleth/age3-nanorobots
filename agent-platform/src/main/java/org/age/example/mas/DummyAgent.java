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

package org.age.example.mas;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.age.compute.mas.agent.AgentBehavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DummyAgent extends AgentBehavior {

	private static final Logger log = LoggerFactory.getLogger(DummyAgent.class);

	private int energy = new Random().nextInt(100_000);

	@Override public void doStep(final int stepNumber) {
		log.debug("Step {}.", stepNumber);
		heavyComputations();
	}

	private void heavyComputations() {
		try {
			TimeUnit.MILLISECONDS.sleep(50L);
		} catch (final InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(final int energy) {
		this.energy = energy;
	}

	@Override public String toString() {
		return toStringHelper(this).add("energy", energy).toString();
	}
}
