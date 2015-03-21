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

package org.age.compute.mas.agent.internal;


import static java.util.Objects.nonNull;

import org.age.compute.mas.agent.Agent;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

@SuppressWarnings("rawtypes")
public final class AgentProxyMethodHandler implements MethodHandler {

	private final Map<String, MethodImplementation> reimplementedMethods;

	public AgentProxyMethodHandler(final InternalAgentView internalAgentView) {
		reimplementedMethods = buildMethodMap(internalAgentView);
	}

	@Override
	public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args)
			throws InvocationTargetException, IllegalAccessException {
		final MethodImplementation methodImplementation = reimplementedMethods.get(
				methodAsString(thisMethod.getName(), thisMethod.getParameterTypes()));
		if (nonNull(methodImplementation)) {
			return methodImplementation.execute(self, proceed, args);
		}
		return proceed.invoke(self, args);
	}

	private static ImmutableMap<String, MethodImplementation> buildMethodMap(
			final InternalAgentView internalAgent) {
		final Void VOID = null;
		return ImmutableMap.<String, MethodImplementation>builder()
						   .put(methodAsString("doStep", int.class), (self, overriddenMethod, args) -> {
							   overriddenMethod.invoke(self, args);
							   internalAgent.doStepOnChildren((Integer)args[0]);
							   return VOID;
						   })
						   .put(methodAsString("addChild", Agent.class), (self, overriddenMethod, args) -> {
							   internalAgent.addChild((Agent)args[0]);
							   return VOID;
						   })
						   .put(methodAsString("removeChild", Agent.class), (self, overriddenMethod, args) -> {
							   internalAgent.removeChild((Agent)args[0]);
							   return VOID;
						   })
						   .put(methodAsString("children"), (self, overriddenMethod, args) -> internalAgent.children())
						   .put(methodAsString("setParent", Agent.class), (self, overriddenMethod, args) -> {
							   internalAgent.setParent((Agent)args[0]);
							   return VOID;
						   })
						   .put(methodAsString("getParent"),
						        (self, overriddenMethod, args) -> internalAgent.getParent())
						   .put(methodAsString("actionsTypes"),
						        (self, overriddenMethod, args) -> internalAgent.actionsTypes())
						   .put(methodAsString("settings"), (self, overriddenMethod, args) -> internalAgent.settings())
						   .put(methodAsString("name"), (self, overriddenMethod, args) -> internalAgent.name())
						   .put(methodAsString("behavior"), (self, overriddenMethod, args) -> internalAgent.behavior())
						   .put(methodAsString("behaviorClass"),
						        (self, overriddenMethod, args) -> internalAgent.behaviorClass())
						   .put(methodAsString("query"), (self, overriddenMethod, args) -> internalAgent.query())
						   .build();
	}

	@FunctionalInterface
	private interface MethodImplementation {
		Object execute(Object self, Method overridden, Object[] args)
				throws InvocationTargetException, IllegalAccessException;
	}

	private static String methodAsString(final String name, final Class<?>... args) {
		assert nonNull(name) && nonNull(args);
		return name + '(' + Joiner.on(",").join(args);
	}
}
