package com.exoplatform.cloudworkspaces.platform.documents;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class RateDocumentTest extends BaseTest {

	static final String SPACE_NAME = "SFDCR";

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
	public void rateDocument() throws Exception {
		CW.TOPMENUS.waitSubLink(DOCUMENTS);
		CW.TOPMENUS.clickOnSublink(DOCUMENTS);
		CW.DOCUMENTS.waitDocumentsOpen();
		CW.DOCUMENTS.uploadDocument(PATH_TO_TEST_FILE, DOCUMENT_NAME);
		CW.DOCUMENTS.clickFileInNodeThree(DOCUMENT_NAME);
		CW.DOCUMENTS.waitDmsMenu();
		assertEquals(CW.DOCUMENTS.getTetxFromDoc(), "Test message content");
		CW.DOCUMENTS.clickOnLink("Vote");
		CW.DOCUMENTS.waitUIPopupWindow();
		CW.DOCUMENTS.clickOnVoteStars("Best");
		CW.DOCUMENTS.waitVotedInfo();
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
