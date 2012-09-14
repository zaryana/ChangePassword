package com.exoplatform.cloudworkspaces.platform.homepage.activitystream;

import org.junit.After;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class PostStatusTest extends BaseTest {

	static final String MESS_TO_STATUSFIELD = "STATUS_FOR_POSTING";

	@Test
	public void postStatus() throws Exception {
		CW.WORKSPACE.postStatusMessage(MESS_TO_STATUSFIELD);
	}

	@After
	public void cleanUp() throws Exception {
		CW.WORKSPACE.deleteStatusMessage(1, MESS_TO_STATUSFIELD);
	}
}
