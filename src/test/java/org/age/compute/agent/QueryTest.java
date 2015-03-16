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

package org.age.compute.agent;

import static org.assertj.core.api.Assertions.assertThat;

import org.age.compute.agent.agent.Agent;
import org.age.compute.agent.agent.AgentBehavior;
import org.age.compute.agent.agent.AgentBuilder;
import org.age.compute.agent.agent.Workplace;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class QueryTest {

	static class AgentWithMagicNumber extends AgentBehavior {

		private int magicNumber;

		private Iterator<Integer> sequence = Collections.<Integer>emptyList().iterator();

		@Override public void doStep(int stepNumber) {
			if (sequence.hasNext()) { magicNumber = sequence.next(); }
		}

		public void setMagicNumberSequence(Integer... sequence) {
			this.sequence = Arrays.asList(sequence).iterator();
		}

		public int getMagicNumber() {
			return magicNumber;
		}
	}

	static class QueryingAgent extends AgentBehavior {

		private AgentWithMagicNumber agentWithGreatestNumber;

		@Override public void doStep(int stepNumber) {
			agentWithGreatestNumber = query(AgentWithMagicNumber.class).max(
					(a1, a2) -> a1.getMagicNumber() - a2.getMagicNumber()).get();
		}

		public AgentWithMagicNumber getAgentWithGreatestNumber() {
			return agentWithGreatestNumber;
		}
	}

	@Test public void querying_shuld_work_in_basic_case() {
		Workplace workplace = new Workplace("", Collections.emptyList());

		Agent<AgentWithMagicNumber> agent1 = AgentBuilder.create(AgentWithMagicNumber.class);
		agent1.behavior().setMagicNumberSequence(1, 2, 3);
		workplace.addChild(agent1);
		agent1.setParent(workplace);

		Agent<AgentWithMagicNumber> agent2 = AgentBuilder.create(AgentWithMagicNumber.class);
		agent2.behavior().setMagicNumberSequence(10, 20, 1);
		workplace.addChild(agent2);
		agent2.setParent(workplace);

		Agent<AgentWithMagicNumber> agent3 = AgentBuilder.create(AgentWithMagicNumber.class);
		agent3.behavior().setMagicNumberSequence(100, 0, 0);
		workplace.addChild(agent3);
		agent3.setParent(workplace);

		Agent<QueryingAgent> agent4 = AgentBuilder.create(QueryingAgent.class);
		workplace.addChild(agent4);
		agent4.setParent(workplace);

		workplace.behavior().doStep(0);
		assertThat(agent4.behavior().getAgentWithGreatestNumber()).isSameAs(agent3.behavior());
		workplace.behavior().doStep(1);
		assertThat(agent4.behavior().getAgentWithGreatestNumber()).isSameAs(agent2.behavior());
		workplace.behavior().doStep(2);
		assertThat(agent4.behavior().getAgentWithGreatestNumber()).isSameAs(agent1.behavior());
	}

}
