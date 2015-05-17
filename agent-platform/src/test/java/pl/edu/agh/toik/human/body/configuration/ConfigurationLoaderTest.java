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

package pl.edu.agh.toik.human.body.configuration;

import org.age.compute.mas.agent.AgentBehavior;

import org.testng.annotations.Test;

public final class ConfigurationLoaderTest {

	public static class SampleAgent extends AgentBehavior {
		@Override public void doStep(final int step) {
			// Empty
		}
	}

	@Test public void test_loadingSampleConfig() {
		ConfigurationLoader.load(getClass().getClassLoader().getResourceAsStream("sample.cfg"));
	}
}