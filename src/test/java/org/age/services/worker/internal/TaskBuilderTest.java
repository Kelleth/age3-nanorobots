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

package org.age.services.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.when;

import org.age.services.worker.FailedComputationSetupException;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TaskBuilderTest {

	@Mock private ListeningScheduledExecutorService executorService;

	@Mock private ListenableScheduledFuture<Object> future;

	@Mock private FutureCallback<Object> callback;

	@BeforeMethod public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test public void testFromClass() {
		final TaskBuilder taskBuilder = TaskBuilder.fromClass(FromClassUtil.class.getCanonicalName());

		assertThat(taskBuilder.className()).isEqualTo(FromClassUtil.class.getCanonicalName());
		assertThat(taskBuilder.springContext()).isNotNull();
		assertThat(taskBuilder.isConfigured()).isFalse();
	}

	@Test(expectedExceptions = FailedComputationSetupException.class) public void testFromClass_notExisting() {
		final TaskBuilder taskBuilder = TaskBuilder.fromClass("org.class.NotExisting");

		taskBuilder.finishConfiguration();
	}

	@Test public void testBuildAndSchedule() {
		when(executorService.schedule(any(Runnable.class), eq(0L), any(TimeUnit.class))).then(RETURNS_MOCKS);
		final TaskBuilder taskBuilder = TaskBuilder.fromClass(FromClassUtil.class.getCanonicalName());

		taskBuilder.finishConfiguration();
		taskBuilder.buildAndSchedule(executorService, callback);
	}

	@Test(expectedExceptions = IllegalStateException.class) public void testBuildAndSchedule_needsToBeConfigured() {
		when(executorService.schedule(any(Runnable.class), eq(0L), any(TimeUnit.class))).then(RETURNS_MOCKS);
		final TaskBuilder taskBuilder = TaskBuilder.fromClass(FromClassUtil.class.getCanonicalName());

		taskBuilder.buildAndSchedule(executorService, callback);
	}

	@Test(expectedExceptions = IllegalStateException.class) public void testCannotConfigureTwoTimes() {
		when(executorService.schedule(any(Runnable.class), eq(0L), any(TimeUnit.class))).then(RETURNS_MOCKS);
		final TaskBuilder taskBuilder = TaskBuilder.fromClass(FromClassUtil.class.getCanonicalName());

		taskBuilder.finishConfiguration();
		taskBuilder.finishConfiguration();
	}

	@Test(expectedExceptions = IllegalStateException.class) public void testCannotUpdateAfterFinishingConfiguration() {
		when(executorService.schedule(any(Runnable.class), eq(0L), any(TimeUnit.class))).then(RETURNS_MOCKS);
		final TaskBuilder taskBuilder = TaskBuilder.fromClass(FromClassUtil.class.getCanonicalName());

		taskBuilder.finishConfiguration();
		taskBuilder.registerSingleton(new Object());
	}

}