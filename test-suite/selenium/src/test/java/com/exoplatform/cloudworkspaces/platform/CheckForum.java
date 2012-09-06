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

import static org.junit.Assert.*;

import com.exoplatform.cw.BaseTest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $Id:
 *
 */
public class CheckForum extends BaseTest
{
   static final String FORUMS_SUBLINK = "Forums";

   static final String DEFAULT_WORKSPACE_SUBLINK = "Default";

   static final String DISKUTIONS = "Discussions";

   static final String TITLE = "New_title";

   static final String NAME_UPLOAD_FILE = "screen.png";

   static final String PAH_TO_ATTACHED_FILE =
      "/home/musienko.maksim/exo-project/cloud-workspace-testsuite-selenium/target/test-classes/org/exoplatform/cw/avatar/screen.png";

   static final String POSTER = USER_NAME;

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
   public void forumTest() throws Exception
   {
      CW.WORKSPACE.waitBasicElements();
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");
      
      CW.TOPMENUS.waitLink(DEFAULT_WORKSPACE_SUBLINK);
      CW.TOPMENUS.clickOnLink(DEFAULT_WORKSPACE_SUBLINK);
      CW.TOPMENUS.waitSubLink(DISKUTIONS);
      CW.TOPMENUS.clickOnSublink(DISKUTIONS);
      CW.FORUM.waitOpen();
      CW.FORUM.clickOnTopic(1);
      CW.FORUM.waitTopicForm();
      CW.FORUM.typeTitleMess(TITLE);
      CW.FORUM.typeMess("New_message");
      CW.FORUM.clickOnSubmitTopicForm();
      CW.FORUM.waitTopicFormClosed();
      CW.FORUM.waitNewLinkAppear(TITLE);
      CW.FORUM.waitNewLinkAppearWithPrefix(POSTER);
      CW.FORUM.clickOnLinkForumPage(TITLE);

      CW.FORUM.waitTopicPage();
      CW.FORUM.waitNewLinkAppear("Post Reply");
      CW.FORUM.clickOnLinkForumPage("Post Reply");
      CW.FORUM.waitPostForm();
      CW.FORUM.typePostFormMess("Reply");
      CW.FORUM.typePostMess("Reply_message");
      CW.FORUM.clickOnSubmitTopicForm();
      CW.FORUM.waitTopicFormClosed();
      CW.FORUM.waitPostDiscutionMess(2);
      assertEquals(CW.FORUM.getTextDisscution(2), "Reply_message");
      CW.FORUM.clickOnButtonInPostMess(2, "Private");
      CW.FORUM.waitPostForm();
      CW.FORUM.typePostFormMess("Prived");
      CW.FORUM.typePostMess("Prived_message");
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
    
      
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");

      CW.TOPMENUS.waitSubLink(DEFAULT_WORKSPACE_SUBLINK);
      CW.TOPMENUS.clickOnLink(DEFAULT_WORKSPACE_SUBLINK);
      CW.TOPMENUS.waitSubLink(DISKUTIONS);
      CW.TOPMENUS.clickOnSublink(DISKUTIONS);
      CW.FORUM.waitOpen();
      CW.FORUM.typeToSearch("Prived\n");
      CW.FORUM.waitNewLinkAppear("Prived");
   }

}
