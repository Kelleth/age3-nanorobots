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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.age.compute.mas.misc.ReflectionUtils.allMethodsAnnotatedBy;

import org.age.compute.mas.InternalAgentRepresentationProxyMethodHandler;
import org.age.compute.mas.action.Action;
import org.age.compute.mas.agent.internal.InternalAgentRepresentation;
import org.age.compute.mas.message.MessageHandler;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public final class AgentBuilder<A extends AgentBehavior> {

	private final Class<A> agentClass;

	private final Map<String, Object> settings = newHashMap();

	private final List<Class<Action>> actions = newArrayList();

	private @Nullable Agent<?> parent;

	private @Nullable String name;

	private AgentBuilder(final Class<A> agentClass) {
		this.agentClass = requireNonNull(agentClass);
	}

	public static <A extends AgentBehavior> AgentBuilder<A> create(final Class<A> agentClass) {
		return new AgentBuilder<>(requireNonNull(agentClass));
	}

	public static <A extends AgentBehavior> AgentBuilder<A> baseOn(final Agent<A> existingAgent) {
		return baseOn(requireNonNull(existingAgent).behavior());
	}

	@SuppressWarnings("unchecked")
	public static <A extends AgentBehavior> AgentBuilder<A> baseOn(final A existingAgent) {
		final InternalAgentRepresentation internalAgentRepresentation = (InternalAgentRepresentation)requireNonNull(
				existingAgent);
		// @formatter:off
		return (AgentBuilder<A>)create(internalAgentRepresentation.behaviorClass())
				.withActions(internalAgentRepresentation.actionsTypes())
				.withParent(internalAgentRepresentation.getParent())
				.withSettings(internalAgentRepresentation.settings());
		// @formatter:on
	}

	public AgentBuilder<A> withSettings(final Map<String, Object> settings) {
		this.settings.putAll(requireNonNull(settings));
		return this;
	}

	public AgentBuilder<A> withParent(final Agent<?> parent) {
		this.parent = parent;
		return this;
	}

	public AgentBuilder<A> withActions(final List<Class<Action>> actions) {
		this.actions.addAll(requireNonNull(actions));
		return this;
	}

	public AgentBuilder<A> withName(final String name) {
		this.name = name;
		return this;
	}

	@SuppressWarnings("unchecked") public Agent<A> build() {
		final InternalAgentRepresentationImpl internalAgent = new InternalAgentRepresentationImpl(actions, settings,
		                                                                                          parent, name);

		try {
			verifyClassCorrectness(agentClass);
			final Class<?> clazz = prepareClassForThisAgent(agentClass);
			final Object proxy = clazz.getConstructor().newInstance();
			((ProxyObject)proxy).setHandler(new InternalAgentRepresentationProxyMethodHandler(internalAgent));

			final A behavior = (A)proxy;
			internalAgent.setSelf(behavior);

			return (Agent<A>)proxy;
		} catch (final Throwable e) {
			throw new AgentInstantiationException(e);
		}
	}

	private static <A extends AgentBehavior> void verifyClassCorrectness(final Class<A> agentClass) {
		verifyMessageHandlers(agentClass);
	}

	private static <A extends AgentBehavior> Class<?> prepareClassForThisAgent(final Class<A> agentClass) {
		final ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(agentClass);
		factory.setInterfaces(new Class[] {InternalAgentRepresentation.class});
		return factory.createClass();
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

}
