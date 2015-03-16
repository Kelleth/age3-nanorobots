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

import static java.util.Arrays.asList;

import org.age.compute.agent.action.Action;
import org.age.compute.agent.agent.Agent;
import org.age.compute.agent.agent.AgentBehavior;
import org.age.compute.agent.configuration.AgentDescriptor;
import org.age.compute.agent.configuration.Configuration;
import org.age.compute.agent.configuration.StopCondition;
import org.age.compute.agent.configuration.WorkplaceDescriptor;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlatformTest {

	public static class DummyAgent extends AgentBehavior {
		@Override public void doStep(int stepNumber) {
		}
	}

	static class SimpleDescriptor implements AgentDescriptor {

		private final String name;

		private Optional<AgentDescriptor> parent;

		private List<AgentDescriptor> children = Collections.emptyList();

		public SimpleDescriptor(String name) {
			this.name = name;
		}

		@Override public Class<? extends AgentBehavior> agentClass() {
			return DummyAgent.class;
		}

		@Override public String name() {
			return name;
		}

		@Override public Optional<AgentDescriptor> parent() {
			return parent;
		}

		@Override public List<AgentDescriptor> children() {
			return children;
		}

		@Override public Map<String, Object> settings() {
			return Collections.emptyMap();
		}

		@Override public List<Class<Action>> actions() {
			return Collections.emptyList();
		}

		public void setParent(AgentDescriptor parent) {
			this.parent = Optional.ofNullable(parent);
		}

		public void setChildren(List<AgentDescriptor> children) {
			this.children = children;
		}
	}

	@Test public void should_instantiate_agents_correctly() {
		Platform platform = new Platform(new Configuration() {

			/*
			simple hierarchy:
				root
			   /    \
			  a      b
			 / \    / \
			a1 a2  b1  b2
			*/
			@Override public List<WorkplaceDescriptor> getWorkplaces() {
				return Collections.singletonList(new WorkplaceDescriptor() {
					@Override public List<AgentDescriptor> getAgents() {
						SimpleDescriptor root = new SimpleDescriptor("root");
						SimpleDescriptor a = new SimpleDescriptor("a");
						SimpleDescriptor b = new SimpleDescriptor("b");
						SimpleDescriptor a1 = new SimpleDescriptor("a1");
						SimpleDescriptor a2 = new SimpleDescriptor("a2");
						SimpleDescriptor b1 = new SimpleDescriptor("b1");
						SimpleDescriptor b2 = new SimpleDescriptor("b2");

						root.setChildren(asList(a, b));
						a.setParent(root);
						b.setParent(root);

						a.setChildren(asList(a1, a2));
						a1.setParent(a);
						a2.setParent(a);

						b.setChildren(asList(b1, b2));
						b1.setParent(b);
						b2.setParent(b);

						return asList(root);
					}

					@Override public List<Class<Action>> getActions() {
						return Collections.emptyList();
					}
				});
			}

			@Override public StopCondition getStopCondition() {
				return null;
			}
		});

		List<Agent<?>> agents = platform.getWorkplaces().get(0).children();
		Assertions.assertThat(agents).hasSize(1);

		Agent<?> root = agents.get(0);

		AgentAssert.assertThat(root).hasName("root").numberOfChildrenEquals(2);

		Agent<?> a = root.children().get(0);
		AgentAssert.assertThat(a).hasName("a").numberOfChildrenEquals(2);

		Agent<?> a1 = a.children().get(0);
		AgentAssert.assertThat(a1).hasName("a1").numberOfChildrenEquals(0);

		Agent<?> a2 = a.children().get(1);
		AgentAssert.assertThat(a2).hasName("a2").numberOfChildrenEquals(0);

		Agent<?> b = root.children().get(1);
		AgentAssert.assertThat(b).hasName("b").numberOfChildrenEquals(2);

		Agent<?> b1 = b.children().get(0);
		AgentAssert.assertThat(b1).hasName("b1").numberOfChildrenEquals(0);

		Agent<?> b2 = b.children().get(1);
		AgentAssert.assertThat(b2).hasName("b2").numberOfChildrenEquals(0);
	}

}
