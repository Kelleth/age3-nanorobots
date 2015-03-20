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


import static java.util.Objects.nonNull;

import org.age.compute.mas.agent.Agent;
import org.age.compute.mas.agent.internal.InternalAgentRepresentation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

@SuppressWarnings("rawtypes")
public final class InternalAgentRepresentationProxyMethodHandler implements MethodHandler {

	private final Map<String, MethodImplementation> reimplementedMethods;

	public InternalAgentRepresentationProxyMethodHandler(
			final InternalAgentRepresentation internalAgentRepresentation) {
		reimplementedMethods = buildMethodMap(internalAgentRepresentation);
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
			final InternalAgentRepresentation enhancedAgent) {
		final Void VOID = null;
		return ImmutableMap.<String, MethodImplementation>builder()
		                   .put(methodAsString("doStep", int.class), (self, overriddenMethod, args) -> {
			                   overriddenMethod.invoke(self, args);
			                   enhancedAgent.doStepOnChildren((Integer)args[0]);
			                   return VOID;
		                   })
		                   .put(methodAsString("addChild", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.addChild((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(methodAsString("removeChild", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.removeChild((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(methodAsString("children"), (self, overriddenMethod, args) -> enhancedAgent.children())
		                   .put(methodAsString("setParent", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.setParent((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(methodAsString("getParent"),
		                        (self, overriddenMethod, args) -> enhancedAgent.getParent())
		                   .put(methodAsString("actionsTypes"),
		                        (self, overriddenMethod, args) -> enhancedAgent.actionsTypes())
		                   .put(methodAsString("settings"), (self, overriddenMethod, args) -> enhancedAgent.settings())
		                   .put(methodAsString("name"), (self, overriddenMethod, args) -> enhancedAgent.name())
		                   .put(methodAsString("behavior"), (self, overriddenMethod, args) -> enhancedAgent.behavior())
		                   .put(methodAsString("behaviorClass"),
		                        (self, overriddenMethod, args) -> enhancedAgent.behaviorClass())
		                   .put(methodAsString("query"), (self, overriddenMethod, args) -> enhancedAgent.query())
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
