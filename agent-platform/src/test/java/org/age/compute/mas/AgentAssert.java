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

package org.age.compute.mas;

import org.age.compute.mas.agent.Agent;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class AgentAssert extends AbstractAssert<AgentAssert, Agent<?>> {

	public AgentAssert(Agent<?> actual) {
		super(actual, AgentAssert.class);
	}

	public static AgentAssert assertThat(Agent<?> actual) {
		return new AgentAssert(actual);
	}

	public AgentAssert hasName(String name) {
		Assertions.assertThat(actual.name()).describedAs("Name is different").isEqualTo(name);
		return this;
	}

	public AgentAssert numberOfChildrenEquals(int number) {
		Assertions.assertThat(actual.children()).describedAs("Different number of children").hasSize(number);
		return this;
	}

}
