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
 * Created: 2014-08-25
 */

package org.age.node;

import org.age.services.lifecycle.internal.DefaultNodeLifecycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Bootstrapper {

	private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

	private Bootstrapper() {
	}

	public static void main(final String... args) {

		DefaultNodeLifecycleService lifecycleService = null;
		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-node.xml")) {
			context.registerShutdownHook();
			lifecycleService = context.getBean(DefaultNodeLifecycleService.class);
			lifecycleService.awaitTermination();
		} finally {
			log.info("Finishing.");
			lifecycleService.awaitTermination();
		}
		log.info("Exiting.");
		System.exit(0);
	}
}
