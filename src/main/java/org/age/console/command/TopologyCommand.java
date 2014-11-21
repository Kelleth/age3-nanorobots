/*
 * Created: 2014-10-16
 * $Id$
 */

package org.age.console.command;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.age.services.topology.TopologyService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting and configuring topology of the cluster.
 */
@Named
@Parameters(commandNames = "topology", commandDescription = "Topology management", optionPrefixes = "--")
public class TopologyCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(TopologyCommand.class);

	@Inject @Named("non-participating") private TopologyService topologyService;

	@Parameter(names = "--info") private boolean info;


	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) {
		if (info) {
			info(printWriter);
		}
		return true;
	}


	private void info(final PrintWriter printWriter) {
		final Optional<String> masterId = topologyService.masterId();
		final Optional<DirectedGraph<String, DefaultEdge>> topology = topologyService.topologyGraph();
		final Optional<String> topologyType = topologyService.topologyType();

		printWriter.println("Topology info = {");
		printWriter.println("\tmaster = " + masterId.orElse("# not elected #"));
		if (topology.isPresent()) {
			printWriter.println("\ttopology type = " + topologyType.get());
			printWriter.println("\ttopology = {");
			for (final DefaultEdge edge : topology.get().edgeSet()) {
				printWriter.println("\t\t" + edge);
			}
			printWriter.println("\t}");
		} else {
			printWriter.println("\ttopology type = # no topology #");
		}
		printWriter.println("}");
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}
}
