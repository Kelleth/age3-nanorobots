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

package org.age.compute.mas.agent;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Objects.requireNonNull;

import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.Workplace.WorkplaceBehavior;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Workplace implements Agent<WorkplaceBehavior>, Runnable {

	private static final Logger log = LoggerFactory.getLogger(Workplace.class);

	private final String name;

	private final List<Action> actions;

	private final WorkplaceBehavior behavior = new WorkplaceBehavior();

	private final List<Agent<?>> children = newLinkedList();

	private final List<Agent<?>> agentsForRemovalAtTheEndOfTurn = newLinkedList();

	public Workplace(final String name, final List<Class<Action>> actions) {
		this.name = requireNonNull(name);
		this.actions = requireNonNull(actions).stream().map(AgentUtils::instantiateSafely).collect(Collectors.toList());
	}

	public class WorkplaceBehavior extends AgentBehavior {
		@Override public void doStep(final int stepNumber) {
			log.debug("Workplace step on children {}.", stepNumber);

			for (final Agent<?> agent : children()) {
				agent.behavior().doStep(stepNumber);
			}

			actions.forEach(action -> action.execute(Workplace.this, ImmutableList.copyOf(children)));
			removeAgents();
		}
	}

	private void removeAgents() {
		agentsForRemovalAtTheEndOfTurn.forEach(children::remove);
		agentsForRemovalAtTheEndOfTurn.clear();
	}

	@Override public List<Agent<?>> children() {
		return ImmutableList.copyOf(children);
	}

	@Override public void addChild(final Agent<?> child) {
		children.add(requireNonNull(child));
	}

	@Override public void addChildren(final Collection<Agent<?>> children) {
		this.children.addAll(requireNonNull(children));
	}

	@Override public void removeChild(final Agent<?> child) {
		agentsForRemovalAtTheEndOfTurn.add(requireNonNull(child));
	}

	@Override public void setParent(final Agent<?> parent) {
		throw new UnsupportedOperationException("Workplaces have no parents.");
	}

	@Override public Agent<?> getParent() {
		throw new UnsupportedOperationException("Workplaces have no parents.");
	}

	@Override public String name() {
		return name;
	}

	@Override public WorkplaceBehavior behavior() {
		return behavior;
	}

	@Override public Class<WorkplaceBehavior> behaviorClass() {
		return WorkplaceBehavior.class;
	}

	@Override public void run() {
		int step = 1;
		while (!Thread.currentThread().isInterrupted()) {
			behavior().doStep(step++);
		}
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(name).toString();
	}
}
