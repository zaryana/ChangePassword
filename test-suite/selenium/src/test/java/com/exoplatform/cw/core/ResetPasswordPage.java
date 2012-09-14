package com.exoplatform.cw.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="riuvshin@exoplatform.com">Roman Iuvshin</a>
 * @version $Id:
 * 
 */
public class ResetPasswordPage extends BaseTest {

	private interface Locators {

		String RESET_FORM = "resetForm";

		String EMAIL_FIELD = "email";

		String RESET_PASSWORD_BTN = "submitButton";

		String REQUEST_COMPLETED_MESSAGE = "//span[text()='Request completed, check your email for instructions.']";
	}

	@FindBy(id = Locators.RESET_FORM)
	private WebElement resetForm;

	@FindBy(id = Locators.EMAIL_FIELD)
	private WebElement emailField;

	@FindBy(id = Locators.RESET_PASSWORD_BTN)
	private WebElement resetPasswordBtn;

	@FindBy(xpath = Locators.REQUEST_COMPLETED_MESSAGE)
	private WebElement requestCompleteMessage;

	/**
	 * wait base elements on staging Login form
	 * 
	 * @throws Exception
	 */
	public void waitAppearResetPasswordPage() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver element) {

				return resetForm.isDisplayed() && emailField != null
						&& resetPasswordBtn.isDisplayed();
			}
		});
	}

	/**
	 * clear input field
	 */
	public void clearInputField() {
		emailField.clear();
	}

	/**
	 * type email for changing user password
	 */
	public void typeEmail() {
		emailField.sendKeys(OWNER_MAIL);
	}

	/**
	 * click on change my password button
	 */
	public void clickOnResetPasswordBtn() {
		resetPasswordBtn.click();
	}

	/**
	 * wait for apper reauest complete message
	 * 
	 * @throws Exception
	 */
	public void waitAppearRequestMessage() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver element) {

				return requestCompleteMessage.isDisplayed();
			}
		});
	}
}
