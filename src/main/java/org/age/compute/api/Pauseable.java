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
 * Created: 2015-01-18.
 */

package org.age.compute.api;

/**
 * A task that can be paused by the platform.
 *
 * This interface should be implemented by all tasks that want to support pausing of the computation.
 *
 * The paused task should not exit the {@link Runnable#run()} method. If it exits the method as a result of the {@link
 * #pause()} call, it will be considered finished.
 */
public interface Pauseable extends Runnable {

	/**
	 * Pauses the task.
	 *
	 * This method is called by the worker service when the "pause" is requested on the computation. It may be executed
	 * in the event-handling thread of the worker service. Thus, it should not block neither perform any long running
	 * operations.
	 */
	void pause();

	/**
	 * Resumes the task.
	 *
	 * This method is called by the worker service when the "resume" is requested on the computation. It may be
	 * executed in the event-handling thread of the worker service. Thus, it should not block neither perform any long
	 * running operations.
	 */
	void resume();

}
