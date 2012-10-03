package com.exoplatform.cloudworkspaces.platform.wiki;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateWikiPageTest.class, EditWikiPageTest.class,
		MoveWikiPageTest.class, SearchWikiPageTest.class })
public class WikiSuite {
}
