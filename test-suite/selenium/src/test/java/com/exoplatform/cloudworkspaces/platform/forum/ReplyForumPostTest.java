package com.exoplatform.cloudworkspaces.platform.forum;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class ReplyForumPostTest extends BaseTest {

	static final String SPACE_NAME = "TSFQR";

	static final String DISCUSSIONS = "Discussions";

	static final String TITLE = "New_title";

	static final String MESSAGE = "New_message";

	static final String QUICK_REPLY_MESSAGE = "this_is_quick_reply";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createOpenSpace(SPACE_NAME);
		CW.TOPMENUS.waitSubLink(DISCUSSIONS);
		CW.TOPMENUS.clickOnSublink(DISCUSSIONS);
		CW.FORUM.waitOpeningForum();
		CW.FORUM.clickOnStartTopic(1);
		CW.FORUM.waitTopicForm();
		CW.FORUM.typeTitleMess(TITLE);
		CW.FORUM.typeMess(MESSAGE);
		CW.FORUM.clickOnSubmitTopicForm();
		CW.FORUM.waitTopicFormClosed();
		CW.FORUM.waitNewLinkAppear(TITLE);
		CW.FORUM.waitNewLinkAppearWithPrefix(TITLE);
		CW.FORUM.clickOnLinkForumPage(TITLE);
		CW.FORUM.waitTopicPage();
		CW.FORUM.checkPostContent(MESSAGE);
	}

	@Test
	public void postReply() throws Exception {
		CW.FORUM.waitNewLinkAppear("Quick Reply");
		CW.FORUM.typeMessageInQuckReplyTextArea(QUICK_REPLY_MESSAGE);
		CW.FORUM.clickOnLinkForumPage("Quick Reply");
		CW.FORUM.checkPostContent(QUICK_REPLY_MESSAGE);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
