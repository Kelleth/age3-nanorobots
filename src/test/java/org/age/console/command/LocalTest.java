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

package org.age.console.command;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeDescriptor;
import org.age.services.identity.NodeIdentityService;
import org.age.services.identity.NodeType;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class LocalTest {

	private static final String NODE_ID = "ID";

	private PrintWriter printWriter;

	private StringWriter stringWriter;

	@Mock private NodeDescriptor identity;

	@Mock private NodeIdentityService identityService;

	@InjectMocks private LocalCommand localCommandCommand;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(identityService.descriptor()).thenReturn(identity);
		when(identity.id()).thenReturn(NODE_ID);
		when(identity.type()).thenReturn(NodeType.UNKNOWN);

		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}

	@Test
	public void testInfo() {
		localCommandCommand.setInfo(true);
		localCommandCommand.execute(null, null, printWriter);

		final String output = stringWriter.toString();

		assertThat(output).contains(NODE_ID);
		assertThat(output).contains(NodeType.UNKNOWN.toString());
	}
}
