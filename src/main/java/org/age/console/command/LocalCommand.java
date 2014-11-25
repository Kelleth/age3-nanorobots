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

import org.age.annotation.ForTestsOnly;
import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import jline.console.ConsoleReader;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Command for getting info about the local node.
 */
@Named
@Parameters(commandNames = "local", commandDescription = "Topology management", optionPrefixes = "--")
public class LocalCommand implements Command {

	private static final Logger log = LoggerFactory.getLogger(LocalCommand.class);

	@Inject private NodeIdentityService identityService;

	@Parameter(names = "--info") private boolean info;

	@Override public boolean execute(@NonNull final JCommander commander, @NonNull final ConsoleReader reader,
	                                 @NonNull final PrintWriter printWriter) {
		if (info) {
			info(printWriter);
		}
		return true;
	}


	private void info(final PrintWriter printWriter) {
		final NodeDescriptor identity = identityService.descriptor();
		printWriter.println("Local node info = {");
		printWriter.println("\tid = " + identity.id());
		printWriter.println("\ttype = " + identity.type());
		printWriter.println("\tservices = " + identity.services());
		printWriter.println("}");
	}

	@Override public String toString() {
		return toStringHelper(this).toString();
	}

	@ForTestsOnly void setInfo(final boolean info) {
		this.info = info;
	}
}
