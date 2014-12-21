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

import static java.util.Objects.isNull;

import org.age.services.lifecycle.NodeLifecycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Bootstrapper {

	private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

	private Bootstrapper() {
	}

	public static void main(final String... args) throws InterruptedException {
		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-node.xml")) {
			context.registerShutdownHook();
			final NodeLifecycleService lifecycleService = context.getBean(NodeLifecycleService.class);
			if (isNull(lifecycleService)) {
				log.error("No node lifecycle service is defined.");
				return;
			}
			lifecycleService.awaitTermination();
		} finally {
			log.info("Finishing.");
		}
		log.info("Exiting.");
		System.exit(0);
	}
}
