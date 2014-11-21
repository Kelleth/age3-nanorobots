package org.age.console.command;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeIdentity;
import org.age.services.identity.NodeIdentityService;
import org.age.services.identity.NodeType;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public final class LocalTest {

	private static final String NODE_ID = "ID";

	private PrintWriter printWriter;

	private StringWriter stringWriter;

	@Mock private NodeIdentity identity;

	@Mock private NodeIdentityService identityService;

	@InjectMocks private LocalCommand localCommandCommand;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(identityService.nodeIdentity()).thenReturn(identity);
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
