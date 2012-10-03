package com.exoplatform.cloudworkspaces.platform.wiki;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.exoplatform.cw.BaseTest;

public class EditWikiPageTest extends BaseTest {

	static final String SPACE_NAME = "SFWEP";

	static final String WIKI = "Wiki";

	static final String WIKI_PAGE_NAME = "Wiki_Page_for_test_EDIT";

	static final String WIKI_PAGE_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
			+ "Vestibulum volutpat sollicitudin nulla, ac pellentesque arcu consequat id. Suspendisse potenti. Sed porta pretium congue.";

	static final String CONTENT_FOR_CHANGE = "Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.";

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
	public void editWikiPage() throws Exception {
		CW.WIKI.clicklOnEditDocMenu();
		CW.WIKI.waitAddWiki();
		CW.WIKI.cliclOnContentDocAddWiki();
		// need for set focus to field
		Thread.sleep(500);
		CW.WIKI.typeTextDoc(Keys.ARROW_DOWN.toString());
		CW.WIKI.typeTextDoc(Keys.ARROW_DOWN.toString());
		CW.WIKI.typeTextDoc(Keys.END.toString());
		CW.WIKI.typeTextDoc(Keys.ENTER.toString());
		CW.WIKI.typeTextDoc(CONTENT_FOR_CHANGE);
		CW.WIKI.clickOnLink("Save");
		CW.WIKI.waitWikiHome();
		CW.WIKI.waitLink(WIKI_PAGE_NAME);
		assertEquals(WIKI_PAGE_NAME, CW.WIKI.getTitleDocOnHomeWiki());
		assertEquals(WIKI_PAGE_CONTENT + "\n" + CONTENT_FOR_CHANGE,
				CW.WIKI.getContentDocHomeWiki());
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
