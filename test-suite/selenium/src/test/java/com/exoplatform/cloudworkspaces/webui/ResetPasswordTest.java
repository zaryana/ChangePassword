package com.exoplatform.cloudworkspaces.webui;

import org.junit.BeforeClass;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="riuvshin@exoplatform.com">Roman Iuvshin</a>
 * @version $Id:
 * 
 */
public class ResetPasswordTest extends BaseTest {

	@Test
	public void changePassTest() throws Exception {
		CW.LOGIN_PAGE.waitAppearLoginPageStaging();
		CW.LOGIN_PAGE.clickOnForgotPasswordBtn();
		CW.RESET_PASSWORD.waitAppearResetPasswordPage();
		CW.RESET_PASSWORD.clearInputField();
		CW.RESET_PASSWORD.typeEmail();
		CW.RESET_PASSWORD.clickOnResetPasswordBtn();
		CW.RESET_PASSWORD.waitAppearRequestMessage();
		driver.get(MAIL_HOST);
		CW.MAIL.waitGmailLoginForm();
	    CW.MAIL.typeLogin("eosroman");
	    CW.MAIL.typePassword("LifeIsFunny76eXo");
	    CW.MAIL.confBtnClick();
	    CW.MAIL.waitSeleniumMail();
	}
}
