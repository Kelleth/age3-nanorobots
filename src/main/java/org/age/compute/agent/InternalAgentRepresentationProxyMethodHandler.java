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

import org.age.compute.agent.agent.Agent;
import org.age.compute.agent.agent.internal.InternalAgentRepresentation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

public class InternalAgentRepresentationProxyMethodHandler implements MethodHandler {

	private final Map<String, MethodImplementation> reimplementedMethods;

	public InternalAgentRepresentationProxyMethodHandler(final InternalAgentRepresentation internalAgentRepresentation)
			throws InvocationTargetException, IllegalAccessException {
		reimplementedMethods = reimplementedMethods(internalAgentRepresentation);
	}

	@Override public Object invoke(final Object self, final Method overridden, final Method forwarder, final Object[] args) throws Throwable {
		final MethodImplementation methodImplementation = reimplementedMethods.get(
				method(overridden.getName(), overridden.getParameterTypes()));
		if (methodImplementation != null) {
			return methodImplementation.execute(self, forwarder, args);
		}
		return forwarder.invoke(self, args);
	}

	private static ImmutableMap<String, MethodImplementation> reimplementedMethods(
			final InternalAgentRepresentation enhancedAgent) throws IllegalAccessException, InvocationTargetException {
		final Void VOID = null;
		return ImmutableMap.<String, MethodImplementation>builder()
		                   .put(method("doStep", int.class), (self, overriddenMethod, args) -> {
			                   overriddenMethod.invoke(self, args);
			                   enhancedAgent.doStepOnChildren((Integer)args[0]);
			                   return VOID;
		                   })
		                   .put(method("addChild", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.addChild((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(method("removeChild", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.removeChild((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(method("children"), (self, overriddenMethod, args) -> enhancedAgent.children())
		                   .put(method("setParent", Agent.class), (self, overriddenMethod, args) -> {
			                   enhancedAgent.setParent((Agent)args[0]);
			                   return VOID;
		                   })
		                   .put(method("getParent"), (self, overriddenMethod, args) -> enhancedAgent.getParent())
		                   .put(method("actionsTypes"),
		                        (self, overriddenMethod, args) -> enhancedAgent.actionsTypes())
		                   .put(method("settings"), (self, overriddenMethod, args) -> enhancedAgent.settings())
		                   .put(method("name"), (self, overriddenMethod, args) -> enhancedAgent.name())
		                   .put(method("behavior"), (self, overriddenMethod, args) -> enhancedAgent.behavior())
		                   .put(method("behaviorClass"),
		                        (self, overriddenMethod, args) -> enhancedAgent.behaviorClass())
		                   .put(method("query"), (self, overriddenMethod, args) -> enhancedAgent.query())
		                   .build();
	}

	private static interface MethodImplementation {
		Object execute(Object self, Method overridden, Object[] args)
				throws InvocationTargetException, IllegalAccessException;
	}

	private static String method(final String name, final Class<?>... args) {
		return name + "(" + Joiner.on(",").join(args);
	}
}
