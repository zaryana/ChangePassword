/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces.platform;

import static org.junit.Assert.assertEquals;

import com.exoplatform.cw.BaseTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $Id:
 *
 */
public class CheckDocuments extends BaseTest
{

   static final String DEFAULT_WORKSPACE_SUBLINK = "Default";

   static final String OPEN_WS_LINK = "Open";

   static final String DOCUMENTS = "Documents";

   static final String FOLDER_NAME = "CwFolder";

   static final String FOLDER_TITLE_2 = "NewFolder2";
   
   static final String FOLDER_TITLE_1 = "NewFolder1";

   static final String FIRST_TEST_FILE =
      "/home/musienko.maksim/exo-project/cloud-workspace-testsuite-selenium/target/test-classes/org/exoplatform/cw/avatar/testUploadodoc1.txt";

   static final String SECOND_TEST_FILE =
      "/home/musienko.maksim/exo-project/cloud-workspace-testsuite-selenium/target/test-classes/org/exoplatform/cw/avatar/testUploadodoc2.txt";

   static final String FIRST_TEST_FILE_NAME = "testUploadodoc1.txt";

   static final String SECOND_TEST_FILE_NAME = "testUploadodoc2.txt";

   @BeforeClass
   public static void testSignUpToWS() throws Exception
   {
      //wait appearance login page and login 
      CW.LOGIN_PAGE.waitAppearLoginPageStaging();
      CW.LOGIN_PAGE.typeUserName();
      CW.LOGIN_PAGE.typeUserPass();
      CW.LOGIN_PAGE.clickOnLoginBtnStg();
   }

   @Test
   public void checkDocuments() throws Exception
   {
      CW.WORKSPACE.waitBasicElements();
      CW.TOPMENUS.mooveMouseToMySpacesMenu();
      CW.TOPMENUS.waitSubLink(OPEN_WS_LINK);
      CW.TOPMENUS.mooveMooseToTopSubMenu(OPEN_WS_LINK);
      CW.TOPMENUS.waitSubLink(DOCUMENTS);
      CW.TOPMENUS.clickOnSublink(DOCUMENTS);
      CW.DOCUMENTS.waitOpen();
      CW.DOCUMENTS.uploadClick();
      CW.DOCUMENTS.waitOpenUploadForm();
      CW.DOCUMENTS.selectIframeUpload();
      CW.DOCUMENTS.typePathToUploadField(FIRST_TEST_FILE);
      selectMainFrame();
      CW.DOCUMENTS.waitUploadFile(FIRST_TEST_FILE_NAME);
      CW.DOCUMENTS.clickOnLink("Save");
      CW.DOCUMENTS.waitClosedUploadForm(FIRST_TEST_FILE_NAME);
      CW.DOCUMENTS.waitUploadManagerForm();
      CW.DOCUMENTS.clickOnLink("Close");
      CW.DOCUMENTS.waitClosedUploadManagerForm();
      CW.DOCUMENTS.waitFileInNodeTree(FIRST_TEST_FILE_NAME);
      CW.DOCUMENTS.clickFileInNodeThree(FIRST_TEST_FILE_NAME);
      CW.DOCUMENTS.waitDmsMenu();
      CW.DOCUMENTS.clickOnLink("Delete");
      CW.DOCUMENTS.waitConfirmDeleteForm();
      CW.DOCUMENTS.clickOnLink("OK");
      CW.DOCUMENTS.waitConfirmDeleteFormClosed();
      CW.DOCUMENTS.waitDeletingFileInNodeTree(FIRST_TEST_FILE_NAME);
      CW.DOCUMENTS.clickOnLink("New Folder");
      CW.DOCUMENTS.waitNewFolderForm();
      CW.DOCUMENTS.typeTitleNewFolderField(FOLDER_TITLE_1);
      CW.DOCUMENTS.typeNameNewFolderField(FOLDER_TITLE_1);
      CW.DOCUMENTS.clickOnLink("Save");
      CW.DOCUMENTS.waitFileInNodeTree(FOLDER_TITLE_1);
      CW.TOPMENUS.mooveMouseToIntranetMenu();
      CW.TOPMENUS.waitSubLink("Documents");
      CW.TOPMENUS.clickOnSublink("Documents");
      CW.DOCUMENTS.waitOpen();
      CW.DOCUMENTS.drivesBtnClick();
      CW.DOCUMENTS.waitDrivesPage();
      CW.FORUM.waitNewLinkAppearWithPrefix("open");
      CW.FORUM.clickOnLinkWithNamePrefixName("open");
      
      CW.DOCUMENTS.waitOpen();
      CW.DOCUMENTS.waitFileInNodeTree("Documents");
      CW.DOCUMENTS.waitFileInNodeTree("SharedData");
      CW.DOCUMENTS.clickFileInNodeThree("SharedData");
      CW.DOCUMENTS.waitFileInNodeTree("Welcome");

      CW.DOCUMENTS.clickOnLink("Upload");;
      CW.DOCUMENTS.waitOpenUploadForm();
      CW.DOCUMENTS.selectIframeUpload();
      CW.DOCUMENTS.typePathToUploadField(SECOND_TEST_FILE);
      selectMainFrame();
      CW.DOCUMENTS.waitUploadFile(SECOND_TEST_FILE_NAME);
      CW.DOCUMENTS.clickOnLink("Save");
      CW.DOCUMENTS.waitClosedUploadForm(SECOND_TEST_FILE_NAME);
      CW.DOCUMENTS.waitUploadManagerForm();
      CW.DOCUMENTS.clickOnLink("Close");
      CW.DOCUMENTS.waitClosedUploadManagerForm();
      CW.DOCUMENTS.waitFileInNodeTree(SECOND_TEST_FILE_NAME);

      CW.DOCUMENTS.clickFileInNodeThree(SECOND_TEST_FILE_NAME);

      CW.DOCUMENTS.waitDocContainer();
      CW.DOCUMENTS.waitDmsMenu();
      assertEquals(CW.DOCUMENTS.getTetxFromDoc(), "content for checking");

      CW.DOCUMENTS.clickFileInNodeThree("Documents");
      CW.DOCUMENTS.waitLinkAppear("New Folder");
      CW.DOCUMENTS.clickOnLink("New Folder");
      CW.DOCUMENTS.waitNewFolderForm();
      CW.DOCUMENTS.typeTitleNewFolderField(FOLDER_TITLE_2);
      CW.DOCUMENTS.typeNameNewFolderField(FOLDER_TITLE_2);
      CW.DOCUMENTS.clickOnLink("Save");
      CW.DOCUMENTS.waitFileInNodeTree(FOLDER_TITLE_2);

   }

}