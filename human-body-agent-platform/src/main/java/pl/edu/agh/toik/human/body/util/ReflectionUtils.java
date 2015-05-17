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

package pl.edu.agh.toik.human.body.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public final class ReflectionUtils {

	private ReflectionUtils() {}

	public static Collection<Method> allMethodsAnnotatedBy(final Class<?> clazz,
	                                                       final Class<? extends Annotation> annotation) {
		requireNonNull(clazz);
		requireNonNull(annotation);

		final Set<Method> methods = newLinkedHashSet();
		for (final Method method : clazz.getMethods()) {
			if (nonNull(method.getAnnotation(annotation))) {
				methods.add(method);
			}
		}
		for (final Method method : clazz.getDeclaredMethods()) {
			if (nonNull(method.getAnnotation(annotation))) {
				methods.add(method);
			}
		}
		return methods;
	}

}
