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
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.internal.InternalAgentRepresentation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import android.annotation.Nullable;

final class InternalAgentRepresentationImpl implements InternalAgentRepresentation {

	private static final Logger log = LoggerFactory.getLogger(InternalAgentRepresentationImpl.class);

	private final List<Agent<?>> children = newArrayList();

	private final String name;

	private final Map<String, Object> settings = newHashMap();

	private final List<Action> actions;

	private @Nullable Agent<?> parent;

	private AgentBehavior self;

	private final List<Agent<?>> agentsForRemovalAtTheEndOfTurn = newLinkedList();

	private final List<Class<Action>> actionsTypes = newArrayList();

	InternalAgentRepresentationImpl(final List<Class<Action>> actionsTypes, final Map<String, Object> settings,
	                                final @Nullable Agent<?> parent, final @Nullable String name) {
		this.actionsTypes.addAll(requireNonNull(actionsTypes));
		this.settings.putAll(settings);
		this.parent = parent;
		this.name = (name != null) ? name : AgentUtils.randomName();

		actions = actionsTypes.stream().map(AgentUtils::instantiateSafely).collect(Collectors.toList());
	}

	@Override public void doStepOnChildren(final int stepNumber) {
		log.debug("{} doing a step on children.", this);
		children.forEach(child -> child.behavior().doStep(stepNumber));
		executeActions();
		removeAgents();
	}

	private void executeActions() {
		log.debug("{} executing actions.", this);
		actions.forEach(action -> action.execute(this, children));
	}

	private void removeAgents() {
		log.debug("{} removing agents.", this);
		agentsForRemovalAtTheEndOfTurn.forEach(children::remove);
		agentsForRemovalAtTheEndOfTurn.clear();
	}

	@Override public void addChild(final Agent<?> child) {
		log.debug("{} adding a new child {}.", this, child);
		children.add(requireNonNull(child));
	}

	@Override public void removeChild(final Agent<?> child) {
		log.debug("{} removing a child {}.", this, child);
		agentsForRemovalAtTheEndOfTurn.add(requireNonNull(child));
	}

	@Override public void addChildren(final Collection<Agent<?>> children) {
		log.debug("{} adding new children {}.", this, children);
		this.children.addAll(requireNonNull(children));
	}

	@Override public List<Agent<?>> children() {
		return ImmutableList.copyOf(children);
	}

	@Override public void setParent(final Agent<?> parent) {
		this.parent = parent;
	}

	// XXX: I do not like this "Nullable" here. Change to null-object?
	@Override public @Nullable Agent<?> getParent() {
		return parent;
	}

	@Override public String name() {
		return name;
	}

	@Override public Stream<AgentBehavior> query() {
		return parent.children().stream().map(a -> a.behavior());
	}

	@Override public List<Class<Action>> actionsTypes() {
		return ImmutableList.copyOf(actionsTypes);
	}

	public Map<String, Object> settings() {
		return ImmutableMap.copyOf(settings);
	}

	/**
	 * Self is an proxy over Agent and EnhancedAgent
	 */
	public void setSelf(final AgentBehavior self) {
		this.self = requireNonNull(self);
	}

	@Override public AgentBehavior behavior() {
		return self;
	}

	@SuppressWarnings("unchecked") @Override public Class<AgentBehavior> behaviorClass() {
		Class clazz = self.getClass();
		while (nonNull(clazz) && clazz.getName().contains("_$$_jvst")) {
			clazz = clazz.getSuperclass();
		}
		return clazz;
	}

	@Override public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof InternalAgentRepresentation)) {
			return false;
		}
		final InternalAgentRepresentationImpl other = (InternalAgentRepresentationImpl)o;
		return Objects.equals(name, other.name);
	}

	@Override public int hashCode() {
		return name.hashCode();
	}

	@Override public String toString() {
		return toStringHelper(this).addValue(name).addValue(behaviorClass()).toString();
	}
}
