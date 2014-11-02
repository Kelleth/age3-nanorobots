package org.age.console.command;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import org.age.services.identity.NodeIdentity;
import org.age.services.identity.NodeIdentityService;
import org.age.services.identity.NodeType;

public class LocalTest {

	private static final String NODE_ID = "ID";

	private PrintWriter printWriter;

	private StringWriter stringWriter;

	@Mock private NodeIdentity identity;

	@Mock private NodeIdentityService identityService;

	@InjectMocks private Local localCommand;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(identityService.getNodeIdentity()).thenReturn(identity);
		when(identity.getId()).thenReturn(NODE_ID);
		when(identity.getType()).thenReturn(NodeType.UNKNOWN);

		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}

	@AfterMethod
	public void tearDown() {

	}

	@Test
	public void testInfo() {
		localCommand.setInfo(true);
		localCommand.execute(null, null, printWriter);

		final String output = stringWriter.toString();

		assertThat(output, containsString(NODE_ID));
		assertThat(output, containsString(NodeType.UNKNOWN.toString()));
	}
}
