package com.exoplatform.cloudworkspaces.platform.wiki;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class MoveWikiPageTest extends BaseTest {

	static final String SPACE_NAME = "SFWMP";

	static final String WIKI = "Wiki";

	static final String WIKI_PAGE_NAME_PARENT = "Wiki_Page_for_test_PARENT";

	static final String WIKI_PAGE_NAME_CHILD = "Wiki_Page_for_test_CHILD";

	static final String WIKI_PAGE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
			+ "Vestibulum volutpat sollicitudin nulla, ac pellentesque arcu consequat id. Suspendisse potenti. Sed porta pretium congue.";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
		CW.TOPMENUS.waitSubLink(WIKI);
		CW.TOPMENUS.clickOnSublink(WIKI);
		CW.WIKI.waitWikiHome();
		CW.WIKI.waitLink("Wiki Home");
		CW.WIKI.waitLink("Sandbox space");
		CW.WIKI.createWikiPage(WIKI_PAGE_NAME_PARENT, WIKI_PAGE_CONTENT);
	}

	@Test
	public void moveWikiPage() throws Exception {
		CW.WIKI.createWikiPage(WIKI_PAGE_NAME_CHILD, WIKI_PAGE_CONTENT);

		CW.WIKI.moveToMoreMenu();
		CW.WIKI.waitLink("Move Page");
		CW.WIKI.clickOnLink("Move Page");
		CW.WIKI.waitMovePageFormOpen();
		CW.WIKI.waitLink("Wiki Home");
		CW.WIKI.clicklOnMoveTreeItem("Wiki Home");
		CW.WIKI.waitSelectRootElemMoveForm();
		CW.WIKI.clickOnLink("Move");
		CW.WIKI.waitMovePageFormClosed();
		CW.WIKI.waitDocInFirstPosition(WIKI_PAGE_NAME_PARENT);
		CW.WIKI.waitDocInSecondPosition(WIKI_PAGE_NAME_CHILD);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
