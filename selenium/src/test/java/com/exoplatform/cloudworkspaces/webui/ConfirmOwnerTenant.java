package com.exoplatform.cloudworkspaces.webui;

import static org.junit.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;
import com.exoplatform.cw.BaseTest;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class ConfirmOwnerTenant extends BaseTest
{
   @Test
   public void testLoginToMail() throws Exception
   {
      //     SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
      //      Date now = new Date();
      //      String date = format.format(now);
      //      System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<:"+now);
      driver.get(MAIL_HOST);
	  CW.MAIL.waitGmailLoginForm();
      CW.MAIL.typeLogin("musienko_maksim");
      CW.MAIL.typePassword("vfrcbv_1978");
      CW.MAIL.confBtnClick();
      CW.MAIL.selectIframeMail();
      CW.MAIL.waitSeleniumMail();
      CW.MAIL.clickOnSeleniumMailLink();
      CW.MAIL.waitWelcomMailForOwner();
      CW.MAIL.clickOnWelcomeCwBeta();
      CW.MAIL.waitOpenedTenantCreatedMail();
      driver.get(CW.MAIL.getTextFromCretionTenantnLink());
      Thread.sleep(1000);
      CW.REGISTRATION.waitRegistrationForm();
      CW.LOGIN_PAGE.waitAppearLoginPageStaging();
   }

}
