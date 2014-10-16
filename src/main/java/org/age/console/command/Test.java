/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Named;

import org.age.services.worker.WorkerMessage;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import jline.console.ConsoleReader;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
@Parameters(commandNames = "test", commandDescription = "Run sample computations", optionPrefixes = "--")
public class Test implements Command {

	@Inject private HazelcastInstance hazelcastInstance;

	@Parameter(names = "--class") private String klass;


	@Override
	public boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter) {
		ITopic<WorkerMessage> topic = hazelcastInstance.getTopic("worker/channel");
		topic.publish(new WorkerMessage(WorkerMessage.Type.LOAD_CLASS, "org.age.example.Simple"));
		return true;
	}

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
