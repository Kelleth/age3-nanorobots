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

import static java.util.Arrays.asList;

import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.Agent;
import org.age.compute.mas.agent.AgentBehavior;
import org.age.compute.mas.configuration.AgentDescriptor;
import org.age.compute.mas.configuration.Configuration;
import org.age.compute.mas.configuration.StopCondition;
import org.age.compute.mas.configuration.WorkplaceDescriptor;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PlatformTest {

	public static class DummyAgent extends AgentBehavior {
		@Override public void doStep(final int stepNumber) {
		}
	}

	static class SimpleDescriptor implements AgentDescriptor {

		private final String name;

		private Optional<AgentDescriptor> parent;

		private List<AgentDescriptor> children = Collections.emptyList();

		public SimpleDescriptor(final String name) {
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

		public void setParent(final AgentDescriptor parent) {
			this.parent = Optional.ofNullable(parent);
		}

		public void setChildren(final List<AgentDescriptor> children) {
			this.children = children;
		}
	}

	@Test public void should_instantiate_agents_correctly() {
		final Platform platform = new Platform(new Configuration() {

			/*
			simple hierarchy:
				root
			   /    \
			  a      b
			 / \    / \
			a1 a2  b1  b2
			*/
			@Override public List<WorkplaceDescriptor> workplaces() {
				return Collections.singletonList(new WorkplaceDescriptor() {
					@Override public List<AgentDescriptor> agents() {
						final SimpleDescriptor root = new SimpleDescriptor("root");
						final SimpleDescriptor a = new SimpleDescriptor("a");
						final SimpleDescriptor b = new SimpleDescriptor("b");
						final SimpleDescriptor a1 = new SimpleDescriptor("a1");
						final SimpleDescriptor a2 = new SimpleDescriptor("a2");
						final SimpleDescriptor b1 = new SimpleDescriptor("b1");
						final SimpleDescriptor b2 = new SimpleDescriptor("b2");

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

					@Override public List<Class<Action>> actions() {
						return Collections.emptyList();
					}
				});
			}

			@Override public StopCondition stopCondition() {
				return null;
			}
		});

		final List<Agent<?>> agents = platform.workplaces().get(0).children();
		Assertions.assertThat(agents).hasSize(1);

		final Agent<?> root = agents.get(0);

		AgentAssert.assertThat(root).hasName("root").numberOfChildrenEquals(2);

		final Agent<?> a = root.children().get(0);
		AgentAssert.assertThat(a).hasName("a").numberOfChildrenEquals(2);

		final Agent<?> a1 = a.children().get(0);
		AgentAssert.assertThat(a1).hasName("a1").numberOfChildrenEquals(0);

		final Agent<?> a2 = a.children().get(1);
		AgentAssert.assertThat(a2).hasName("a2").numberOfChildrenEquals(0);

		final Agent<?> b = root.children().get(1);
		AgentAssert.assertThat(b).hasName("b").numberOfChildrenEquals(2);

		final Agent<?> b1 = b.children().get(0);
		AgentAssert.assertThat(b1).hasName("b1").numberOfChildrenEquals(0);

		final Agent<?> b2 = b.children().get(1);
		AgentAssert.assertThat(b2).hasName("b2").numberOfChildrenEquals(0);
	}

}
