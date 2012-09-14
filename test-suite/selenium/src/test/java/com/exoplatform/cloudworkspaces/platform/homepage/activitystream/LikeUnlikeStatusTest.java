package com.exoplatform.cloudworkspaces.platform.homepage.activitystream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class LikeUnlikeStatusTest extends BaseTest {

	static final String MESS_TO_STATUSFIELD = "STATUS_FOR_LIKE_UNLIKE";

	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.postStatusMessage(MESS_TO_STATUSFIELD);
	}

	@Test
	public void likeUnlikeStatus() throws Exception {
		CW.WORKSPACE.clickOnLikeLink(1);
		CW.WORKSPACE.waitLikeAppear(1);
		CW.WORKSPACE.clickOnUnlikeLink(1);
		CW.WORKSPACE.verifyThatUnlikeDisappear(1);
	}

	@After
	public void cleanUp() throws Exception {
		CW.WORKSPACE.deleteStatusMessage(1, MESS_TO_STATUSFIELD);
	}
}
