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

package org.age.compute.agent.agent;

import org.age.compute.agent.action.Action;
import org.age.compute.agent.agent.internal.InternalAgentRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class InternalAgentRepresentationImpl implements InternalAgentRepresentation {

	private static final Logger log = LoggerFactory.getLogger(InternalAgentRepresentationImpl.class);

	private final List<Agent<?>> children = new LinkedList<>();

	private final String name;

	private final Map<String, Object> settings;

	private final List<Action> actions;

	private Agent parent;

	private AgentBehavior self;

	private final List<Agent> agentsForRemovalAtTheEndOfTurn = new LinkedList<>();

	private final List<Class<Action>> actionsTypes;

	public InternalAgentRepresentationImpl(final List<Class<Action>> actionsTypes, final Map<String, Object> settings,
	                                       final Agent<?> parent, final String name) {
		this.actionsTypes = actionsTypes;
		this.actions = actionsTypes.stream().map(AgentUtils::instantiateSafely).collect(Collectors.toList());
		this.settings = settings;
		this.parent = parent;
		this.name = name != null ? name : AgentUtils.randomName();
	}

	public void doStepOnChildren(final int stepNumber) {
		children.forEach(child -> child.behavior().doStep(stepNumber));
		executeActions();
		removeAgents();
	}

	private void executeActions() {
		actions.forEach(action -> action.execute(this, children));
	}

	private void removeAgents() {
		agentsForRemovalAtTheEndOfTurn.forEach(children::remove);
		agentsForRemovalAtTheEndOfTurn.clear();
	}

	@Override public void addChild(final Agent<?> child) {
		children.add(child);
	}

	@Override public void removeChild(final Agent<?> child) {
		agentsForRemovalAtTheEndOfTurn.add(child);
	}

	@Override public void addChildren(final Collection<Agent<?>> children) {
		this.children.addAll(children);
	}

	@Override public List<Agent<?>> children() {
		return children;
	}

	@Override public void setParent(final Agent<?> parent) {
		this.parent = parent;
	}

	@Override public Agent<?> getParent() {
		return parent;
	}

	@Override public String name() {
		return name;
	}

	@Override public Stream<AgentBehavior> query() {
		return getParent().children().stream().map(a -> a.behavior());
	}

	@Override public List<Class<Action>> actionsTypes() {
		return actionsTypes;
	}

	public Map<String, Object> settings() {
		return settings;
	}

	@Override public boolean equals(final Object o) {
		if (this == o) { return true; }
		if (!(o instanceof InternalAgentRepresentation)) { return false; }
		final InternalAgentRepresentationImpl that = (InternalAgentRepresentationImpl)o;
		return name.equals(that.name);
	}

	@Override public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Self is an proxy over Agent and EnhancedAgent
	 */
	public void setSelf(final AgentBehavior self) {
		this.self = self;
	}

	@Override public AgentBehavior behavior() {
		return self;
	}

	@SuppressWarnings("unchecked") @Override public Class<AgentBehavior> behaviorClass() {
		Class clazz = self.getClass();
		while (clazz != null && clazz.getName().contains("_$$_jvst")) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

}
