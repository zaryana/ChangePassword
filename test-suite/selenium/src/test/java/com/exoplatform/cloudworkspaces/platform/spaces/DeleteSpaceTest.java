package com.exoplatform.cloudworkspaces.platform.spaces;

import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class DeleteSpaceTest extends BaseTest {

	static final String SPACE_NAME = "DEL";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@Test
	public void deleteSpace() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
