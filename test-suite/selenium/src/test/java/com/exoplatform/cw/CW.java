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
package com.exoplatform.cw;

import com.exoplatform.cw.core.MailValidate;
import com.exoplatform.cw.core.Calendar;
import com.exoplatform.cw.core.CwLoginPage;
import com.exoplatform.cw.core.Documents;
import com.exoplatform.cw.core.ForumPage;
import com.exoplatform.cw.core.MyProfilePage;
import com.exoplatform.cw.core.MySpacePage;
import com.exoplatform.cw.core.SignUpPage;
import com.exoplatform.cw.core.TopMenusPage;
import com.exoplatform.cw.core.Wiki;
import com.exoplatform.cw.core.WorkSpacePage;
import com.exoplatform.cw.core.SignUpDonePage;
import com.exoplatform.cw.core.HeaderUIElements;
import com.exoplatform.cw.core.FooterUIElements;
import com.exoplatform.cw.core.RegistrationWorkspacePage;
import com.exoplatform.cw.core.RegistrationDonePage;
import com.thoughtworks.selenium.Selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * 
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class CW
{

   private Selenium selenium;

   private WebDriver driver;

   private static CW instance;

   public static CW getInstance()
   {
      return instance;
   }

   public SignUpPage SIGNUPPAGE;

   public CwLoginPage LOGIN_PAGE;

   public WorkSpacePage WORKSPACE;

   public MyProfilePage PROFILE;

   public TopMenusPage TOPMENUS;

   public MySpacePage MYSPACE;

   public Calendar CALENDAR;

   public ForumPage FORUM;

   public Documents DOCUMENTS;

   public Wiki WIKI;

   public SignUpDonePage SIGNUPDONE;

   public HeaderUIElements HEADER;

   public FooterUIElements FOOTER;

   public MailValidate MAIL;

   public RegistrationWorkspacePage REGISTRATION;

   public RegistrationDonePage REGISTRATIONDONE;

   public CW(Selenium selenium, String workspaceURL, WebDriver driver)
   {
      this.selenium = selenium;
      this.workspaceURL = workspaceURL;
      this.driver = driver;
      instance = this;

      SIGNUPPAGE = PageFactory.initElements(driver, SignUpPage.class);
      LOGIN_PAGE = PageFactory.initElements(driver, CwLoginPage.class);
      WORKSPACE = PageFactory.initElements(driver, WorkSpacePage.class);
      PROFILE = PageFactory.initElements(driver, MyProfilePage.class);
      TOPMENUS = PageFactory.initElements(driver, TopMenusPage.class);
      MYSPACE = PageFactory.initElements(driver, MySpacePage.class);
      CALENDAR = PageFactory.initElements(driver, Calendar.class);
      FORUM = PageFactory.initElements(driver, ForumPage.class);
      DOCUMENTS = PageFactory.initElements(driver, Documents.class);
      WIKI = PageFactory.initElements(driver, Wiki.class);
      SIGNUPDONE = PageFactory.initElements(driver, SignUpDonePage.class);
      FOOTER = PageFactory.initElements(driver, FooterUIElements.class);
      HEADER = PageFactory.initElements(driver, HeaderUIElements.class);
      MAIL = PageFactory.initElements(driver, MailValidate.class);
      REGISTRATION = PageFactory.initElements(driver, RegistrationWorkspacePage.class);
      REGISTRATIONDONE = PageFactory.initElements(driver, RegistrationDonePage.class);
   }

   public Selenium getSelenium()
   {
      return selenium;
   }

   public WebDriver driver()
   {
      return driver;
   }

   /**
    * Select main frame of IDE.
    * 
    * This method is used, after typing text in editor. To type text you must select editor iframe. After typing, to return to
    * them main frame, use selectMainFrame()
    * 
    */
   public void selectMainFrame()
   {
      driver().switchTo().defaultContent();
   }

   private String workspaceURL;

   public void setWorkspaceURL(String workspaceURL)
   {
      this.workspaceURL = workspaceURL;
   }

   public String getWorkspaceURL()
   {
      return workspaceURL;
   }

}
