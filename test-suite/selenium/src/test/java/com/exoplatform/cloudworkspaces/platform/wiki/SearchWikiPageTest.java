package com.exoplatform.cloudworkspaces.platform.wiki;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class SearchWikiPageTest extends BaseTest {

	static final String SPACE_NAME = "SFWS";

	static final String WIKI = "Wiki";

	static final String WIKI_PAGE_NAME = "Wiki_Page_for_test";

	static final String WIKI_PAGE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
			+ "Vestibulum volutpat sollicitudin nulla, ac pellentesque arcu consequat id. Suspendisse potenti. Sed porta pretium congue.";

	static final String SEARCH_VALUE = "Lorem";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
		CW.TOPMENUS.waitSubLink(WIKI);
		CW.TOPMENUS.clickOnSublink(WIKI);
		CW.WIKI.waitWikiHome();
		CW.WIKI.waitLink("Wiki Home");
		CW.WIKI.waitLink("Sandbox space");
		CW.WIKI.createWikiPage(WIKI_PAGE_NAME, WIKI_PAGE_CONTENT);
	}

	@Test
	public void searchWikiPage() throws Exception {
		CW.WIKI.searchWikiPage(SEARCH_VALUE + "\n");
		CW.WIKI.waitSearchResult(WIKI_PAGE_NAME);

	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
