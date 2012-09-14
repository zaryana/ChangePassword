package com.exoplatform.cloudworkspaces.platform.homepage.activitystream;

import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class DeleteStatusTest extends BaseTest {

	static final String MESS_TO_STATUSFIELD = "STATUS_FOR_DELETING";

	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.postStatusMessage(MESS_TO_STATUSFIELD);
	}

	@Test
	public void deleteStatus() throws Exception {
		CW.WORKSPACE.deleteStatusMessage(1, MESS_TO_STATUSFIELD);

	}

}
