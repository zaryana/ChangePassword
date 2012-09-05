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
import org.openqa.selenium.Keys;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $Id:
 *
 */

public class CheckWiki extends BaseTest
{

   static final String OPEN_WS_LINK = "Open";

   static final String WIKI = "Wiki";

   static final String NAME_WIKI_DOC_1 = "DocWiki_1";
   
   static final String NAME_WIKI_DOC_2 = "DocWiki_2";

   static final String CONTENT_WIKI_DOC_1 =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"
         + "Vestibulum volutpat sollicitudin nulla, ac pellentesque arcu consequat id. Suspendisse potenti. Sed porta pretium congue.";
   
   static final String CONTENT_WIKI_DOC_2 ="some text";


   static final String CONTENT_WIKI_DOC_1_ADD =
      "Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.";

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
   public void CheckWikiPage() throws Exception
   {
      //step 1 goto on open ws -> wiki and check bases elements 
      //on wiki home page
      
      CW.TOPMENUS.mooveMooseToTopSubMenu("Browse");
      CW.TOPMENUS.waitSubLink("Spaces");
      CW.TOPMENUS.clickOnSublink("Spaces");

      CW.TOPMENUS.waitLink(OPEN_WS_LINK);
      CW.TOPMENUS.clickOnLink(OPEN_WS_LINK);
      CW.TOPMENUS.waitSubLink(WIKI);
      CW.TOPMENUS.clickOnSublink(WIKI);
      CW.WIKI.waitWikiHome();
      CW.WIKI.waitLink("Wiki Home");
      CW.WIKI.waitLink("Sandbox space");

      //step2 create new wiki doc and validation 
      CW.WIKI.moveToAddPageMenu();
      CW.WIKI.waitLink("Blank Page");
      CW.WIKI.clickOnLink("Blank Page");
      
      CW.WIKI.waitAddWiki();
      CW.WIKI.typeNameWikiDoc(NAME_WIKI_DOC_1);
      CW.WIKI.typeTextDoc(CONTENT_WIKI_DOC_1);
      CW.WIKI.clickOnLink("Save");
      CW.WIKI.waitWikiHome();
      CW.WIKI.waitLink("DocWiki_1");
      assertEquals(NAME_WIKI_DOC_1, CW.WIKI.getTitleDocOnHomeWiki());
      assertEquals(CONTENT_WIKI_DOC_1, CW.WIKI.getContentDocHomeWiki());

      //step3 edit content
      CW.WIKI.clicklOnEditDocMenu();
      CW.WIKI.waitAddWiki();
      CW.WIKI.cliclOnContentDocAddWiki();
      //need for set focus to field
      Thread.sleep(500);
      CW.WIKI.typeTextDoc(Keys.ARROW_DOWN.toString());
      CW.WIKI.typeTextDoc(Keys.ARROW_DOWN.toString());
      CW.WIKI.typeTextDoc(Keys.END.toString());
      CW.WIKI.typeTextDoc(Keys.ENTER.toString());
      CW.WIKI.typeTextDoc(CONTENT_WIKI_DOC_1_ADD);
      CW.WIKI.clickOnLink("Save");
      CW.WIKI.waitWikiHome();
      CW.WIKI.waitLink("DocWiki_1");
      assertEquals(NAME_WIKI_DOC_1, CW.WIKI.getTitleDocOnHomeWiki());
      assertEquals(CONTENT_WIKI_DOC_1 + "\n" + CONTENT_WIKI_DOC_1_ADD, CW.WIKI.getContentDocHomeWiki());

      //step4 Check watch/unwatch
      CW.WIKI.moveToMoreMenu();
      CW.WIKI.waitLink("Watch");
      CW.WIKI.clickOnLink("Watch");
      CW.WIKI.watchDialogOpen();
      CW.WIKI.clickOnLink("OK");
      CW.WIKI.watchDialogclose();
      CW.WIKI.waitWikiHome();
      CW.WIKI.moveToMoreMenu();
      CW.WIKI.waitLink("Stop Watching");
      CW.WIKI.clickOnLink("Stop Watching");
      CW.WIKI.watchDialogOpen();
      CW.WIKI.clickOnLink("OK");
      CW.WIKI.watchDialogclose();
      CW.WIKI.moveToMoreMenu();
      CW.WIKI.waitLink("Watch");

      //step5 Move created wiki page.
      //create second document for check move of the document function
      CW.WIKI.moveToAddPageMenu();
      CW.WIKI.waitLink("Blank Page");
      CW.WIKI.clickOnLink("Blank Page");
      CW.WIKI.waitAddWiki();
      CW.WIKI.typeNameWikiDoc(NAME_WIKI_DOC_2);
      CW.WIKI.typeTextDoc(CONTENT_WIKI_DOC_2);
      CW.WIKI.clickOnLink("Save");
      CW.WIKI.waitWikiHome();
      CW.WIKI.waitLink(NAME_WIKI_DOC_2);
      CW.WIKI.waitLink(NAME_WIKI_DOC_1);
      assertEquals(NAME_WIKI_DOC_2, CW.WIKI.getTitleDocOnHomeWiki());
      assertEquals(CONTENT_WIKI_DOC_2, CW.WIKI.getContentDocHomeWiki());
     
      //step 6 move documents
      CW.WIKI.moveToMoreMenu();
      CW.WIKI.waitLink("Move Page");
      CW.WIKI.clickOnLink("Move Page");
      CW.WIKI.waitMovePageFormOpen();
      CW.WIKI.waitLink("Wiki Home");
      CW.WIKI.clicklOnMoveTreeItem("Wiki Home");
      CW.WIKI.waitSelectRootElemMoveForm();
      CW.WIKI.clickOnLink("Move");
      CW.WIKI.waitMovePageFormClosed();
      CW.WIKI.waitDocInFirstPosition(NAME_WIKI_DOC_1);
      CW.WIKI.waitDocInSecondPosition(NAME_WIKI_DOC_2);
   }
  
}