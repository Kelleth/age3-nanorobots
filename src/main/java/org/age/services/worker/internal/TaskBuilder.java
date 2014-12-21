/*
 * Copyright (C) 2014 Intelligent Information Systems Group.
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

/*
 * Created: 20.12.14.
 */

package org.age.services.worker.internal;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

import org.age.services.worker.FailedComputationSetupException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Builds a single compute task.
 *
 * It is responsible for data consistency of the task.
 */
@ThreadSafe
final class TaskBuilder {

	private static final Logger log = LoggerFactory.getLogger(TaskBuilder.class);

	private final AtomicBoolean configured = new AtomicBoolean(false);

	private final String className;

	private final AbstractApplicationContext springContext;

	private TaskBuilder(final @NonNull String className, final @NonNull AbstractApplicationContext springContext) {
		assert nonNull(className) && nonNull(springContext);

		this.className = className;
		this.springContext = springContext;
	}

	static @NonNull TaskBuilder fromClass(final @NonNull String className) {
		assert nonNull(className);

		try {
			log.debug("Setting up task from class {}.", className);

			log.debug("Creating internal Spring context.");
			final AnnotationConfigApplicationContext taskContext = new AnnotationConfigApplicationContext();

			// Configure task
			final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(className);
			taskContext.registerBeanDefinition("runnable", builder.getBeanDefinition());

			log.debug("Task setup finished.");

			return new TaskBuilder(className, taskContext);
		} catch (final BeanCreationException e) {
			log.error("Cannot create the task from class.", e);
			throw new FailedComputationSetupException("Cannot create the task from class", e);
		}
	}

	static @NonNull TaskBuilder fromConfig(final @NonNull String configPath) {
		assert nonNull(configPath);

		try {
			log.debug("Setting up task from config {}.", configPath);

			log.debug("Creating internal Spring context.");
			final FileSystemXmlApplicationContext taskContext = new FileSystemXmlApplicationContext(configPath);

			log.debug("Task setup finished.");

			return new TaskBuilder(taskContext.getType("runnable").getCanonicalName(), taskContext);
		} catch (final BeanCreationException e) {
			log.error("Cannot create the task from file.", e);
			throw new FailedComputationSetupException("Cannot create the task from file", e);
		}
	}

	boolean isConfigured() {
		return configured.get();
	}

	@NonNull String className() {
		return className;
	}

	@NonNull AbstractApplicationContext springContext() {
		return springContext;
	}

	void registerSingleton(final @NonNull Object bean) {
		assert nonNull(bean);
		checkState(!isConfigured(), "Task is already configured.");

		springContext.getBeanFactory().registerSingleton(bean.getClass().getSimpleName(), bean);
	}

	void finishConfiguration() {
		checkState(!isConfigured(), "Task is already configured.");

		try {
			assert !configured.get();
			springContext.refresh();
			configured.set(true);
		} catch (final BeansException e) {
			log.error("Cannot refresh the Spring context.", e);
			throw new FailedComputationSetupException("Cannot refresh the Spring context", e);
		}
	}

	StartedTask buildAndSchedule(final @NonNull ListeningScheduledExecutorService executorService,
	                             final @NonNull FutureCallback<Object> executionListener) {
		assert nonNull(executorService) && nonNull(executionListener);
		checkState(isConfigured(), "Task is not configured.");

		try {
			final Runnable runnable = (Runnable)springContext.getBean("runnable");
			log.info("Starting execution of {}.", runnable);
			final ListenableScheduledFuture<?> future = executorService.schedule(runnable, 0L, TimeUnit.SECONDS);
			Futures.addCallback(future, executionListener);
			return new StartedTask(className, springContext, runnable, future);
		} catch (final BeansException e) {
			log.error("Cannot get runnable from the context.", e);
			throw new FailedComputationSetupException("Cannot get runnable from the context.", e);
		}
	}

	@Override public String toString() {
		return toStringHelper(this).add("classname", className).add("configured", configured.get()).toString();
	}
}
