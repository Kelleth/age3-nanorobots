/*
 * Created: 2014-08-25
 * $Id$
 */

package org.age.console;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.age.services.lifecycle.NodeLifecycleService;

public final class Bootstrapper {

	private static final Logger log = LoggerFactory.getLogger(Bootstrapper.class);

	private Bootstrapper() {
	}

	public static void main(final String... args) {

		NodeLifecycleService lifecycleService = null;
		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-console.xml")) {
			lifecycleService = context.getBean(NodeLifecycleService.class);
			context.registerShutdownHook();

			log.info("Starting console.");
			context.getBean(Console.class).mainLoop();

		} catch (final IOException e) {
			log.error("Console exception.", e);

		} finally {
			log.info("Finishing.");

			log.debug("Waiting for NodeLifecycleService to terminate.");
			lifecycleService.awaitTermination();
		}
		log.info("Exiting.");
		System.exit(0);
	}
}
