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

import static org.age.compute.agent.misc.ReflectionUtils.allMethodsAnnotatedBy;

import org.age.compute.agent.InternalAgentRepresentationProxyMethodHandler;
import org.age.compute.agent.action.Action;
import org.age.compute.agent.agent.internal.InternalAgentRepresentation;
import org.age.compute.agent.exception.AgentInstantiationException;
import org.age.compute.agent.message.MessageHandler;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class AgentBuilder {

	public static <A extends AgentBehavior> Builder<A> builder(final Class<A> agentClass) {
		return new Builder<>(agentClass);
	}

	public static <A extends AgentBehavior> Builder<A> baseOn(final Agent<A> existingAgent) {
		return baseOn(existingAgent.behavior());
	}

	@SuppressWarnings("unchecked") public static <A extends AgentBehavior> Builder<A> baseOn(final A existingAgent) {
		final InternalAgentRepresentation internalAgentRepresentation = (InternalAgentRepresentation)existingAgent;
		return (Builder<A>)builder(internalAgentRepresentation.behaviorClass()).withActions(
				internalAgentRepresentation.actionsTypes()).withParent(internalAgentRepresentation.getParent())
		                                                                          .withSettings(
				                                                                          internalAgentRepresentation.settings());
	}

	public static class Builder<A extends AgentBehavior> {

		private final Class<A> agentClass;

		private Map<String, Object> settings = Collections.emptyMap();

		private List<Class<Action>> actions = Collections.emptyList();

		private Agent<?> parent;

		private String name;

		public Builder(final Class<A> agentClass) {
			this.agentClass = agentClass;
		}

		public Builder<A> withSettings(final Map<String, Object> settings) {
			this.settings = settings;
			return this;
		}

		public Builder<A> withActions(final List<Class<Action>> actions) {
			this.actions = actions;
			return this;
		}

		public Builder<A> withParent(final Agent<?> parent) {
			this.parent = parent;
			return this;
		}

		public Builder<A> withName(final String name) {
			this.name = name;
			return this;
		}

		public Agent<A> build() {
			return AgentBuilder.create(agentClass, settings, actions, parent, name);
		}
	}

	@Deprecated public static <A extends AgentBehavior> Agent<A> create(final Class<A> agentClass) {
		return create(agentClass, Collections.emptyMap(), Collections.emptyList(), null, null);
	}

	private static <A extends AgentBehavior> Agent<A> create(final Class<? extends AgentBehavior> agentClass,
	                                                         final Map<String, Object> settings, final List<Class<Action>> actions,
	                                                         final Agent<?> parent, final String name) {
		final InternalAgentRepresentationImpl enhancedAgent = new InternalAgentRepresentationImpl(actions, settings, parent,
		                                                                                    name);

		try {
			verifyClassCorrectness(agentClass);
			final Class clazz = prepareClassForThisAgent(agentClass);
			final Object instance = clazz.newInstance();
			((ProxyObject)instance).setHandler(new InternalAgentRepresentationProxyMethodHandler(enhancedAgent));

			@SuppressWarnings("unchecked") final A behavior = (A)instance;
			enhancedAgent.setSelf(behavior);

			@SuppressWarnings("unchecked") final Agent<A> agent = (Agent<A>)instance;
			return agent;
		} catch (final Throwable e) {
			throw new AgentInstantiationException(e);
		}
	}

	private static <A extends AgentBehavior> void verifyClassCorrectness(final Class<A> agentClass) {
		verifyMessageHandlers(agentClass);
	}

	private static <A extends AgentBehavior> void verifyMessageHandlers(final Class<A> agentClass) {
		for (final Method declaredHandler : allMethodsAnnotatedBy(agentClass, MessageHandler.class)) {
			if (declaredHandler.getParameterCount() != 1) {
				throw new AgentInstantiationException(
						MessageFormat.format("Invalid message handler: `{0}` declared in class: `{1}`",
						                     declaredHandler.getName(), declaredHandler.getDeclaringClass().getName()));
			}
		}
	}

	private static <A extends AgentBehavior> Class prepareClassForThisAgent(final Class<A> agentClass) {
		final ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(agentClass);
		factory.setInterfaces(new Class[] {InternalAgentRepresentation.class});
		return factory.createClass();
	}

}
