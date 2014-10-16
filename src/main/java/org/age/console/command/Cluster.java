/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import java.io.PrintWriter;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.age.services.discovery.HazelcastDiscoveryService;
import org.age.services.identity.NodeIdentity;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import jline.console.ConsoleReader;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
@Parameters(commandNames = "cluster", commandDescription = "Topology management", optionPrefixes = "--")
public class Cluster implements Command {

	@Inject private HazelcastDiscoveryService discoveryService;

	@Parameter(names = "--nodes") private boolean nodes;

	@Parameter(names = "--destroy") private boolean destroy;

	@Override
	public boolean execute(final JCommander commander, final ConsoleReader reader, final PrintWriter printWriter) {
		if (nodes) {
			nodes(printWriter);
		}
		return true;
	}


	private void nodes(final PrintWriter printWriter) {
		final Set<NodeIdentity> neighbours = discoveryService.getMembers();
		for (final NodeIdentity neighbour : neighbours) {
			printWriter.println(neighbour);
		}
	}

	@Override
	public String toString() {
		return toStringHelper(this).toString();
	}
}
