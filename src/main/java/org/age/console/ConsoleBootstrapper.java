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

package org.age.console;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.age.services.lifecycle.NodeLifecycleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Bootstraper for the console.
 */
public final class ConsoleBootstrapper {

	private static final Logger log = LoggerFactory.getLogger(ConsoleBootstrapper.class);

	private ConsoleBootstrapper() {}

	public static void main(final String... args) throws InterruptedException {
		NodeLifecycleService lifecycleService = null;

		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-console.xml")) {
			context.registerShutdownHook();
			lifecycleService = context.getBean(NodeLifecycleService.class);
			if (isNull(lifecycleService)) {
				log.error("No node lifecycle service is defined.");
				return;
			}

			log.info("Starting console.");
			final Console console = context.getBean(Console.class);
			if (isNull(console)) {
				log.error("No console is defined.");
				return;
			}
			console.mainLoop();
		} catch (final IOException e) {
			log.error("Console exception.", e);
		} finally {
			log.info("Finishing.");

			log.debug("Waiting for NodeLifecycleService to terminate.");
			if (nonNull(lifecycleService)) {
				lifecycleService.awaitTermination();
			}
		}
		log.info("Exiting.");
		System.exit(0);
	}
}
