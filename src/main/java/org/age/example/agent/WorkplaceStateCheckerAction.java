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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class WorkplaceStateCheckerAction implements Action {

	private static final Logger logger = LoggerFactory.getLogger(WorkplaceStateCheckerAction.class);

	@Override public void execute(Agent<?> parent, Collection<Agent<?>> agents) {
		logger.info("We have " + agents.size() + " agents currently");
	}
}
