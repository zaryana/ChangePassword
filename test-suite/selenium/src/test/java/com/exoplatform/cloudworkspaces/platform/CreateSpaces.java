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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $Id:
 *
 */
public class CreateSpaces extends BaseTest
{

   static final String MESS_TO_STATUSFIELD = "Lorem Ipsum";

   static final String LINK_SECOND_USER_FIRST_SPACE = "Request to Join";

   static final String LINK_SECOND_USER_SECOND_SPACE = "Join";

   static final String PATH_TO_AVATAR_ICON =
      "/home/musienko.maksim/exo-project/cloud-workspace-testsuite-selenium/src/test/resources/org/exoplatform/cw/avatar/avatar.jpg";

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
   public void createNewDefaultSpace() throws Exception
   {
      CW.WORKSPACE.waitBasicElements();
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");
      
      CW.MYSPACE.waitAddLink();
      CW.MYSPACE.clickAddNewSpace();
      CW.MYSPACE.waitNewSpaceFormAppear();
      CW.MYSPACE.typeSpaceName(DEFAULT_WORKSPASE);
      CW.MYSPACE.clickCreateBtn();
      CW.MYSPACE.waitNewSpaceFormDisAppear();
      CW.MYSPACE.waitElementsAfterCreateWs();
      assertEquals(CW.WORKSPACE.getTitlemess(1), DEFAULT_WORKSPASE);
   }

   @Test
   public void createOpenSpace() throws Exception
   {
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");
      
      CW.MYSPACE.waitAddLink();
      CW.MYSPACE.waitFirstSpaceAppear(DEFAULT_WORKSPASE);
      CW.MYSPACE.waitAddLink();
      CW.MYSPACE.clickAddNewSpace();
      CW.MYSPACE.waitNewSpaceFormAppear();
      CW.MYSPACE.typeSpaceName(OPEN_WORKSPASE);
      CW.MYSPACE.clickOnTabCreateWSForm("Access & Edit");
      CW.MYSPACE.waitOpenSpaseRadoiBtnAppear();
      CW.MYSPACE.clickOnOpenWsRadioBtn();
      //need for select radiobtn
      Thread.sleep(1000);
      CW.MYSPACE.isRadiobtnOpenIsSelect();
      //need for appearance createBtn
      Thread.sleep(2000);
      CW.MYSPACE.clickCreateBtn();
      CW.MYSPACE.waitNewSpaceFormDisAppear();
      CW.MYSPACE.waitElementsAfterCreateWs();
      CW.WORKSPACE.waitMessage(1);
      assertEquals(CW.WORKSPACE.getTitlemess(1), OPEN_WORKSPASE);
     
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");
      
      CW.MYSPACE.waitAddLink();
      CW.MYSPACE.waitFirstSpaceAppear(DEFAULT_WORKSPASE);
      CW.MYSPACE.waitSecondSpaceAppear(OPEN_WORKSPASE);
   }

}
