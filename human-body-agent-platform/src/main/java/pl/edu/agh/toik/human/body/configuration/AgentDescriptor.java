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

package pl.edu.agh.toik.human.body.configuration;

import pl.edu.agh.toik.human.body.Action;

import java.util.List;
import java.util.Map;

/**
 * Agent descriptor contains the configuration of an agent.
 */
public interface AgentDescriptor {

    enum AgentClass {
        BLOODSTREAM_AGENT,
        HUMAN_TISSUE_AGENT
    }

    AgentClass agentClass();

    String name();

    Map<String, Object> settings();

    List<Class<Action>> actions();

}
