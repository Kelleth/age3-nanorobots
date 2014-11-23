/*
 * Created: 2014-08-25
 * $Id$
 */

package org.age.console;

import static java.util.Objects.nonNull;

import org.age.services.lifecycle.internal.DefaultNodeLifecycleService;

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

	public static void main(final String... args) {
		DefaultNodeLifecycleService lifecycleService = null;

		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-console.xml")) {
			lifecycleService = context.getBean(DefaultNodeLifecycleService.class);
			context.registerShutdownHook();

			log.info("Starting console.");
			context.getBean(Console.class).mainLoop();

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
	}
}
