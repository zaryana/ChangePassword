package com.exoplatform.cloudworkspaces.platform.forum;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class ReplyPrivateForumPostTest extends BaseTest {

	static final String SPACE_NAME = "TSFQRP";

	static final String DISCUSSIONS = "Discussions";

	static final String TITLE = "New_title";

	static final String MESSAGE = "New_message";

	static final String PRIVATE_MESSAGE = "this_is_private_message";

	static final File file = new File(
			"src/test/resources/org/exoplatform/cw/avatar/screen.png");

	static final String PAH_TO_ATTACHED_FILE = file.getAbsolutePath();

	static final String NAME_UPLOAD_FILE = "screen.png";

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
	public void postReplyPrivate() throws Exception {
		CW.FORUM.clickOnButtonInPostMess(1, "Private");
		CW.FORUM.waitPostForm();
		CW.FORUM.typePostFormMess(PRIVATE_MESSAGE);
		CW.FORUM.typePostMess(PRIVATE_MESSAGE);
		CW.FORUM.clickOnLinkForumPage("Attach files");
		CW.FORUM.waitUploadForm();
		CW.FORUM.typeToUplodField(PAH_TO_ATTACHED_FILE);
		CW.FORUM.waitFileNameOnUploadForm(NAME_UPLOAD_FILE);
		CW.FORUM.clickOnLinkForumPage("Save");
		CW.FORUM.waitCloseUploadForm();
		CW.FORUM.clickOnLinkForumPage("Submit");
		CW.FORUM.waitPostFormClosed();
		CW.FORUM.waitAttacmentTitleImage(NAME_UPLOAD_FILE);
		CW.FORUM.waitAttacmentImage(NAME_UPLOAD_FILE);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
