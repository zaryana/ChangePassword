package com.exoplatform.cloudworkspaces.platform.wiki;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CreateWikiPageTest extends BaseTest {

	static final String SPACE_NAME = "SFWCP";

	static final String WIKI = "Wiki";

	static final String WIKI_PAGE_NAME = "Wiki_Page_for_test";

	static final String WIKI_PAGE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
			+ "Vestibulum volutpat sollicitudin nulla, ac pellentesque arcu consequat id. Suspendisse potenti. Sed porta pretium congue.";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@Test
	public void createWikiPage() throws Exception {
		CW.TOPMENUS.waitSubLink(WIKI);
		CW.TOPMENUS.clickOnSublink(WIKI);
		CW.WIKI.waitWikiHome();
		CW.WIKI.waitLink("Wiki Home");
		CW.WIKI.waitLink("Sandbox space");
		CW.WIKI.createWikiPage(WIKI_PAGE_NAME, WIKI_PAGE_CONTENT);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
