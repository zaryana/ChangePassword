package com.exoplatform.cw.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

public class MailValidate extends BaseTest {

	private interface Locators {
		String IFRAME_MAIL_ID = "canvas_frame";

		String LOGIN_FIELD_ID = "j_username";

		String PASSWORD_FIELD_ID = "j_password";

		String LOGIN_BTN_CLASS = "button";

		String SELENIUM_MAIL = "/html/body/div/div[2]/div/div[2]/div/div/div/div[2]/div/div/div[2]/div/div/div[5]/div/div[6]/div/div/div[2]/span/a";
		// "//div[@class='TK']//div[@class='aim']//div[@class='TO']//span[@class='nU n1']/a[contains(text(), 'selenium')]";

		String CLOUD_WORKSPACE_BETA_MAIL = "//tbody/tr[1]//span[@class='zF' and text()='Cloud Workspaces Beta']";

		String CLOUD_WORKSPACE_WELCOME_MAIL = "//tbody/tr[1]//span[@id]/b[contains(.,'Welcome to the exoplatform Social Intranet')]";

		String LINK_TO_REGISTRATION_EXOPLATFORM_CW = "/registration.jsp";

		String LINK_TO_CREATED_TENANT_FOR_OWNER = "http://cloud-intranet";

		String WELCOME_MAIL = "//div[@id=':7t']//table";

	}

	@FindBy(id = Locators.LOGIN_FIELD_ID)
	private WebElement loginField;

	@FindBy(id = Locators.PASSWORD_FIELD_ID)
	private WebElement passField;

	@FindBy(className = Locators.LOGIN_BTN_CLASS)
	private WebElement btnLogin;

	@FindBy(id = Locators.IFRAME_MAIL_ID)
	private WebElement iframeWithMails;

	@FindBy(xpath = Locators.SELENIUM_MAIL)
	private WebElement seleniumMail;

	@FindBy(xpath = Locators.CLOUD_WORKSPACE_BETA_MAIL)
	private WebElement checkBetaMessage;

	@FindBy(xpath = Locators.CLOUD_WORKSPACE_BETA_MAIL)
	private WebElement checkWelcomeMessage;

	@FindBy(xpath = Locators.WELCOME_MAIL)
	private WebElement welcomeMail;

	@FindBy(partialLinkText = Locators.LINK_TO_REGISTRATION_EXOPLATFORM_CW)
	private WebElement linkToCw;

	@FindBy(partialLinkText = Locators.LINK_TO_CREATED_TENANT_FOR_OWNER)
	private WebElement linkToTenant;

	/**
	 * wait base elements of sign form
	 * 
	 * @throws Exception
	 */
	public boolean isMailLoginFormPresent() throws Exception {
		return loginField.isDisplayed() && passField.isDisplayed()
				&& btnLogin.isDisplayed();
	}

	public void typeLogin(String login) {
		loginField.sendKeys(login);
	}

	public void typePassword(String pass) {
		passField.sendKeys(pass);
	}

	public void confBtnClick() {
		btnLogin.click();
	}

	/**
	 * wait body container
	 * 
	 * @throws Exception
	 */
	public void waitGmailLoginForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return loginField != null && loginField.isDisplayed()
						&& passField != null && passField.isDisplayed();
			}
		});
	}

	/**
	 * wait body container
	 * 
	 * @throws Exception
	 */
	public void waitIframeWithMail() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return iframeWithMails != null
							&& iframeWithMails.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait body container
	 * 
	 * @throws Exception
	 */
	public void waitSeleniumMail() throws Exception {
		new WebDriverWait(driver, 600).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return seleniumMail != null && seleniumMail.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait mail with welcome to Cloud Work space Beta
	 * 
	 * @throws Exception
	 */
	public void waitWelcomMailForOwner() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return checkBetaMessage != null
						&& checkBetaMessage.isDisplayed();
			}
		});
	}

	/**
	 * wait opened exo welcome mail
	 * 
	 * @throws Exception
	 */
	public void waitOpenedWelcomeMail() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return linkToCw != null && linkToCw.isDisplayed();
			}
		});
	}

	/**
	 * wait opened exo welcome mail
	 * 
	 * @throws Exception
	 */
	public void waitOpenedTenantCreatedMail() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return linkToTenant != null && linkToTenant.isDisplayed();
			}
		});
	}

	/**
	 * wait opened exo welcome mail
	 * 
	 * @throws Exception
	 */
	public void clickOnLinkInInvitationMail() throws Exception {
		linkToCw.click();
	}

	/**
	 * click on selenium link-filter
	 * 
	 * @throws Exception
	 */
	public void clickOnSeleniumMailLink() throws Exception {

		seleniumMail.click();
	}

	/**
	 * return text wit location from link
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getTextFromRegistrationLink() throws Exception {

		return linkToCw.getText();
	}

	/**
	 * return text wit location from link
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getTextFromCretionTenantnLink() throws Exception {

		return linkToTenant.getText();
	}

	/**
	 * click on selenium link-filter
	 * 
	 * @throws Exception
	 */
	public void clickOnWelcomeToCwLink() throws Exception {

		seleniumMail.click();

	}

	/**
	 * click on selenium link-filter
	 * 
	 * @throws Exception
	 */
	public void clickOnWelcomeCwBeta() throws Exception {

		checkWelcomeMessage.click();

	}

	public void selectIframeMail() throws Exception {
		waitIframeWithMail();
		driver.switchTo().frame(iframeWithMails);
	}

}