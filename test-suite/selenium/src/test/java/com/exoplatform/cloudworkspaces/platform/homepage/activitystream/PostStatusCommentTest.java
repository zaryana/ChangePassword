package com.exoplatform.cloudworkspaces.platform.homepage.activitystream;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class PostStatusCommentTest extends BaseTest {
	static final String MESS_TO_STATUSFIELD = "STATUS_FOR_COMMENT";
	static final String MESS_TO_COMMENT = "THIS_IS_THE_COMMENT";

	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.postStatusMessage(MESS_TO_STATUSFIELD);
	}

	@Test
	public void statusComment() throws Exception {
		CW.WORKSPACE.waitCommentLinkAppear(1);
		Thread.sleep(3000);
		CW.WORKSPACE.clickComment(1);
		CW.WORKSPACE.waitCommentBox(1);
		CW.WORKSPACE.typeComment(1, MESS_TO_COMMENT);
		CW.WORKSPACE.waitSendCommentBtn(1);
		CW.WORKSPACE.sendComment(1);
		CW.WORKSPACE.waitPostComment(1);
		CW.WORKSPACE.waitMessageAfterCommClick(1);
		Thread.sleep(5000);
		assertEquals(MESS_TO_COMMENT, CW.WORKSPACE.getComment(1));
	}

	@After
	public void cleanUp() throws Exception {
		CW.WORKSPACE.deleteCommentBtn(1);
		driver.switchTo().alert().accept();
		Thread.sleep(3000);
		CW.WORKSPACE.deleteStatusMessage(1, MESS_TO_STATUSFIELD);
	}
}
