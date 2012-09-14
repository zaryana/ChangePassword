package com.exoplatform.cloudworkspaces.platform.spaces;

import org.junit.After;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CreateSpaceWithValidationTest extends BaseTest {

	static final String SPACE_NAME = "VALID";

	@Test
	public void createSpaceWithValidation() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
