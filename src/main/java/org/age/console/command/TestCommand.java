/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

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

	private static final String EXAMPLES_PACKAGE = "org.age.example.";

	private static final Logger log = LoggerFactory.getLogger(TestCommand.class);

	@Inject private HazelcastInstance hazelcastInstance;

	@Parameter(names = "--list-examples") private boolean listExamples;

	@Parameter(names = "--example") private String example;

	@MonotonicNonNull private ITopic<WorkerMessage<?>> topic;

	@PostConstruct private void construct() {
		topic = hazelcastInstance.getTopic("worker/channel");
	}

	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) {
		if (listExamples) {
			listExamples(printWriter);
			return true;
		}

		final String className = EXAMPLES_PACKAGE + example;

		topic.publish(WorkerMessage.createBroadcastWithPayload(WorkerMessage.Type.LOAD_CLASS, className));
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (final InterruptedException e) {
			log.debug("Interrupted.", e);
		}
		topic.publish(WorkerMessage.createBroadcastWithoutPayload(WorkerMessage.Type.START_COMPUTATION));
		return true;
	}

	private static void listExamples(final PrintWriter printWriter) {
		try {
			final ClassPath classPath = ClassPath.from(TestCommand.class.getClassLoader());
			final ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClasses(EXAMPLES_PACKAGE);
			classes.forEach(klass -> printWriter.println(klass.getSimpleName()));
		} catch (final IOException e) {
			log.error("Cannot load classes.", e);
			printWriter.println("Error: Cannot load classes.");
		}
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
