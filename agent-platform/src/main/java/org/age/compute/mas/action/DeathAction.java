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

package org.age.compute.mas.action;

import org.age.compute.mas.agent.Agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public abstract class DeathAction implements Action {

	private static final Logger log = LoggerFactory.getLogger(DeathAction.class);

	@Override public final void execute(final Agent<?> parent, final Collection<Agent<?>> agents) {
		log.debug("Removing agents from {}.", parent);
		agents.stream().filter(this::shouldDie).forEach(parent::removeChild);
	}

	protected abstract boolean shouldDie(Agent<?> agent);
}
