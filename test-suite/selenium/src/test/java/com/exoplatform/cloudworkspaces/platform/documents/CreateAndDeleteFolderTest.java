package com.exoplatform.cloudworkspaces.platform.documents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CreateAndDeleteFolderTest extends BaseTest {

	static final String SPACE_NAME = "SFDCF";

	static final String FOLDER_NAME = "new_created_folder";

	static final String DOCUMENTS = "Documents";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@Test
	public void createAndDeleteFolder() throws Exception {
		CW.TOPMENUS.waitSubLink(DOCUMENTS);
		CW.TOPMENUS.clickOnSublink(DOCUMENTS);
		CW.DOCUMENTS.waitDocumentsOpen();
		CW.DOCUMENTS.clickOnLink("New Folder");
		CW.DOCUMENTS.waitNewFolderForm();
		CW.DOCUMENTS.typeTitleNewFolderField(FOLDER_NAME);
		CW.DOCUMENTS.typeNameNewFolderField(FOLDER_NAME);
		CW.DOCUMENTS.clickOnLink("Save");
		CW.DOCUMENTS.waitFileInNodeTree(FOLDER_NAME);
		CW.DOCUMENTS.clickFileInNodeThree(FOLDER_NAME);
		CW.DOCUMENTS.waitDeleteFolderButton();
		CW.DOCUMENTS.deleteFolder();
		CW.DOCUMENTS.waitConfirmDeleteForm();
		CW.DOCUMENTS.clickOnLink("OK");
		CW.DOCUMENTS.waitConfirmDeleteFormClosed();
		CW.DOCUMENTS.waitDeletingFileInNodeTree(FOLDER_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.SPACES.deleteSpace(SPACE_NAME);
	}

}
