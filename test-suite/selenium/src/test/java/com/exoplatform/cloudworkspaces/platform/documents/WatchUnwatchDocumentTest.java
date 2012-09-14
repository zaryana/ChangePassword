package com.exoplatform.cloudworkspaces.platform.documents;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class WatchUnwatchDocumentTest extends BaseTest {

	static final String SPACE_NAME = "SFDWUW";

	static final String DOCUMENTS = "Documents";

	static final File File = new File(
			"src/test/resources/org/exoplatform/cw/avatar/testUploadodoc1.txt");

	static final String PATH_TO_TEST_FILE = File.getAbsolutePath();

	static final String DOCUMENT_NAME = File.getName();

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@Test
	public void watchUnwatchDocumentTest() throws Exception {
		CW.TOPMENUS.waitSubLink(DOCUMENTS);
		CW.TOPMENUS.clickOnSublink(DOCUMENTS);
		CW.DOCUMENTS.waitDocumentsOpen();
		CW.DOCUMENTS.uploadDocument(PATH_TO_TEST_FILE, DOCUMENT_NAME);
		CW.DOCUMENTS.clickFileInNodeThree(DOCUMENT_NAME);
		CW.DOCUMENTS.waitDmsMenu();
		assertEquals(CW.DOCUMENTS.getTetxFromDoc(), "Test message content");
		CW.DOCUMENTS.clickOnWatchUnwatchButton();
		CW.DOCUMENTS.waitPopUpButton("Watch");
		CW.DOCUMENTS.clickOnPopUpButton("Watch");
		CW.DOCUMENTS.waitPopUpContent("You are watching this document.");
		CW.DOCUMENTS.clickOnPopUpButton("OK");
		CW.DOCUMENTS.waitDmsMenu();
		CW.DOCUMENTS.clickOnWatchUnwatchButton();
		CW.DOCUMENTS.waitPopUpButton("Unwatch");
		CW.DOCUMENTS.clickOnPopUpButton("Unwatch");
		CW.DOCUMENTS
				.waitPopUpContent("You have left the watchers list successfully.");
		CW.DOCUMENTS.clickOnPopUpButton("OK");
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
