/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.age.services.discovery.DiscoveryService;
import org.age.services.identity.NodeDescriptor;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting info about and managing the whole cluster.
 */
@Named
@Parameters(commandNames = "cluster", commandDescription = "Cluster management", optionPrefixes = "--")
public class ClusterCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(ClusterCommand.class);

	@Inject private DiscoveryService discoveryService;

	@Parameter(names = "--nodes") private boolean nodes;

	@Parameter(names = "--destroy") private boolean destroy;

	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) {
		if (nodes) {
			nodes(printWriter);
		}
		return true;
	}


	private void nodes(final PrintWriter printWriter) {
		log.debug("Printing information about nodes.");
		final Set<NodeDescriptor> neighbours = discoveryService.allMembers();
		neighbours.forEach(printWriter::println);
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
