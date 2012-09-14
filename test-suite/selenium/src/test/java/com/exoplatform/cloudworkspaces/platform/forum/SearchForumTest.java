package com.exoplatform.cloudworkspaces.platform.forum;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class SearchForumTest extends BaseTest {

	static final String SPACE_NAME = "TSFS";

	static final String DISCUSSIONS = "Discussions";

	static final String TITLE = "New_title_for_search";

	static final String MESSAGE = "New_message_for_search";

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
	public void searchTest() throws Exception {
		CW.FORUM.typeToSearch(MESSAGE + "\n");
		CW.FORUM.waitNewLinkAppear(TITLE);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
