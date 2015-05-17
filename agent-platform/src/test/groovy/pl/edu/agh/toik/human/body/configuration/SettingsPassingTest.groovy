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

package pl.edu.agh.toik.human.body.configuration

import org.age.compute.mas.Platform
import org.age.compute.mas.agent.AgentBehavior
import org.assertj.core.api.Assertions
import org.testng.annotations.Test

class SettingsPassingTest {

	static final KEY1 = "foo"
	static final VALUE1 = 1
	static final VALUE2 = "some property"
	static final KEY2 = "bar"

	static Map<String, Object> actualSettings

	static class A extends AgentBehavior {

		@Override
		void doStep(int stepNumber) {
			SettingsPassingTest.actualSettings = settings()
		}
	}

	@Test
	void should_pass_settings_properly() {
		final configuration = ConfigurationLoader.load("""
			final SETTINGS = [
				"$KEY1": $VALUE1,
				"$KEY2": "$VALUE2",
			]

			configuration {
				computationDurationInSeconds = 1

				workplace {
					agent {
						agentClass = org.age.compute.mas.configuration.SettingsPassingTest.A
						settings = SETTINGS
					}
				}
			}
			 """)

		final platform = new Platform(configuration)
		platform.run()

		Assertions.assertThat(platform.workplaces().get(0).children().get(0).settings())
				.hasSize(2)
				.containsEntry(KEY1, VALUE1)
				.containsEntry(KEY2, VALUE2)
	}

}
