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

package org.age.example.mas;

import org.age.compute.mas.Platform;
import org.age.compute.mas.configuration.Configuration;
import org.age.compute.mas.configuration.ConfigurationLoader;

public class Computation implements Runnable {

	@Override public void run() {
		Configuration configuration = ConfigurationLoader.loadFromClassPath("org/age/example/agent/computation.cfg");
		Platform platform = new Platform(configuration);
		try {
			platform.run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
