/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;

import org.age.services.worker.WorkerMessage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command that eases testing the cluster.
 */
@Named
@Scope("prototype")
@Parameters(commandNames = "test", commandDescription = "Run sample computations", optionPrefixes = "--")
public class TestCommand implements Command {

	private static final String EXAMPLES_PACKAGE = "org.age.example";

	private static final Logger log = LoggerFactory.getLogger(TestCommand.class);

	@Inject private HazelcastInstance hazelcastInstance;

	@Parameter(names = "--list-examples") private boolean listExamples;

	@Parameter(names = "--example") private String example;

	@Parameter(names = "--config") private String config;

	@MonotonicNonNull private ITopic<WorkerMessage<?>> topic;

	@PostConstruct private void construct() {
		topic = hazelcastInstance.getTopic("worker/channel");
	}

	@Override public boolean execute(final @NonNull JCommander commander, final @NonNull ConsoleReader reader,
	                                 final @NonNull PrintWriter printWriter) {
		if (listExamples) {
			listExamples(printWriter);
		} else if (!isNullOrEmpty(example)) {
			runExample();
		} else if (!isNullOrEmpty(config)) {
			runConfig(printWriter);
		}
		return true;
	}

	private static void listExamples(final @NonNull PrintWriter printWriter) {
		log.debug("Listing examples.");
		try {
			final ClassPath classPath = ClassPath.from(TestCommand.class.getClassLoader());
			final ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(EXAMPLES_PACKAGE);
			log.debug("Class path {}.", classes);
			classes.forEach(klass -> printWriter.println(klass.getSimpleName()));
		} catch (final IOException e) {
			log.error("Cannot load classes.", e);
			printWriter.println("Error: Cannot load classes.");
		}
	}

	private void runExample() {
		log.debug("Running example.");
		final String className = EXAMPLES_PACKAGE + '.' + example;

		topic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CLASS, className));
		try {
			TimeUnit.SECONDS.sleep(1L);
		} catch (final InterruptedException e) {
			log.debug("Interrupted.", e);
		}
		topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));
	}

	private void runConfig(final @NonNull PrintWriter printWriter) {
		log.debug("Running config.");
		final Path path = Paths.get(config);
		if (!Files.exists(path)) {
			printWriter.println("File " + config + " does not exist.");
			return;
		}
		topic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CONFIGURATION,
		                                                       path.normalize().toString()));
		try {
			TimeUnit.SECONDS.sleep(1L);
		} catch (final InterruptedException e) {
			log.debug("Interrupted.", e);
		}
		topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
