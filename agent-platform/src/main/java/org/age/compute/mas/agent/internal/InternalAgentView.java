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

package org.age.compute.mas.agent.internal;

import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.Agent;
import org.age.compute.mas.agent.AgentBehavior;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This interface represents internal structure of an agent.
 *
 * While setting up platform for new computation, we connect {@link AgentBehavior}s (created by user)
 * with {@link Agent} class in {@link AgentBuilder} using proxy. This interface should not be used
 * by the end user and it should not be used in platform internal code unless really needed.
 */
public interface InternalAgentView extends Agent<AgentBehavior> {

	void doStepOnChildren(int stepNumber);

	Map<String, Object> settings();

	Stream<AgentBehavior> query();

	List<Class<Action>> actionsTypes();

}
