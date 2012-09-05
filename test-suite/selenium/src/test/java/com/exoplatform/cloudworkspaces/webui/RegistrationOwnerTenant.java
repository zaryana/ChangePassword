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
package com.exoplatform.cloudworkspaces.webui;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import com.exoplatform.cw.BaseMail;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 *
 */
public class RegistrationOwnerTenant extends BaseMail
{

   private static final String HEADER_SIGN_UP_PAGE = "Cloud Workspaces\n" + "The Free Social Intranet for Your Company";

   @Test
   public void testLoginToMail() throws Exception
   {
      //     SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
      //      Date now = new Date();
      //      String date = format.format(now);
      //      System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<:"+now);
      CW.MAIL.waitGmailLoginForm();
      CW.MAIL.typeLogin("musienko_maksim");
      CW.MAIL.typePassword("vfrcbv_1978");
      CW.MAIL.confBtnClick();
      CW.MAIL.selectIframeMail();
      CW.MAIL.waitSeleniumMail();
      CW.MAIL.clickOnSeleniumMailLink();
      CW.MAIL.waitWelcomMailForOwner();
      CW.MAIL.clickOnWelcomeCwBeta();
      CW.MAIL.waitOpenedWelcomeMail();
      Thread.sleep(1000);
      driver.get(CW.MAIL.getTextFromRegistrationLink());
      CW.REGISTRATION.waitRegistrationForm();
   }

   @Test
   public void registraTionTest() throws Exception
   {
      CW.REGISTRATION.waitRegistrationForm();
      CW.REGISTRATION.typeToFirstNameField("maxura");
      CW.REGISTRATION.typeToLastNameField("max");
      CW.REGISTRATION.typeToPhoneField("(0472)4546343");
      CW.REGISTRATION.typeToCompanyField("eXo");
      CW.REGISTRATION.typeToPasswordField("qwertyuio");
      CW.REGISTRATION.typeToConfirmPassField("qwertyuio");
      CW.REGISTRATION.clickOnSubmitBtn();

      //wait and check signup-done page
      CW.SIGNUPDONE.waitBodyContainer();
      CW.HEADER.waitHeaderElements();
      CW.FOOTER.waitFooterElements();
      assertEquals(CW.HEADER.getHeader(), HEADER_SIGN_UP_PAGE);
      assertThat(CW.SIGNUPDONE.getAllTextFromBodyContainer().split("\n")).contains(
         "Thank you for your interest in Cloud Workspaces", "Watch the Getting Started video:",
         "Even more to explore:", "Read an introduction to the key features of Cloud Workspaces",
         "Join the eXo Community to connect with other users, access documentation, forums and more",
         "Want to host your own social intranet on-premise or in a private cloud? Learn more about eXo Platform 3.5");
   }

}