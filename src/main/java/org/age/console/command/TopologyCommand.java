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
 * Created: 2014-10-16
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
