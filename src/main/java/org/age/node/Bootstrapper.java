/*
 * Created: 2014-08-25
 * $Id$
 */

package org.age.node;

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
		try (final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-node.xml")) {
			context.registerShutdownHook();
			lifecycleService = context.getBean(NodeLifecycleService.class);
			lifecycleService.awaitTermination();
		} finally {
			log.info("Finishing.");
			lifecycleService.awaitTermination();
		}
		log.info("Exiting.");
		System.exit(0);
	}
}
