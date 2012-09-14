package com.exoplatform.cloudworkspaces.platform.spaces;

import org.junit.After;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CreateOpenSpaceTest extends BaseTest {

	static final String SPACE_NAME = "OPEN";

	@Test
	public void createOpenSpace() throws Exception {
		CW.SPACES.createOpenSpace(SPACE_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
