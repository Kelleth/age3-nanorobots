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

package pl.edu.agh.toik.human.body.agent;

import java.util.Collections;
import java.util.Map;

/**
 * Class that describes agent behavior.
 * <p>
 * In order to instantiate a new agent based on a behavior, the programmer needs to use {@link
 * org.age.compute.mas.agent.internal.AgentBuilder}.
 */
public abstract class AgentBehavior {

    /**
     * Method run by an agent in each step. It needs to be overridden in subclasses.
     * <p>
     * Code executed in this method should not take too much time. The intention of "stepped" agents is to
     * have short computations block run repeatedly.
     */
    public abstract void doStep(int stepNumber);

    /**
     * Returns settings set for an agent.
     * <p>
     * This implementations does nothing - it is handled by a proxy class.
     * In case of wrong usage (when the class was not created by {@link org.age.compute.mas.agent.internal.AgentBuilder}
     * an empty map is returned.
     */
    protected Map<String, Object> settings() {
        return Collections.emptyMap();
    }

}

