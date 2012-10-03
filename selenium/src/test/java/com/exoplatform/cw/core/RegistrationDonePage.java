package com.exoplatform.cw.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

public class RegistrationDonePage extends BaseTest {
	// Locators basic elements Dialog About Menu
	private interface Locators {
		String PAGE_BODI_CONTAINER = "div.ThanksPages";
	}

	@FindBy(css = Locators.PAGE_BODI_CONTAINER)
	private WebElement bodyContainer;

	/**
	 * wait body container
	 * 
	 * @throws Exception
	 */
	public void waitHeaderElements() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return bodyContainer != null && bodyContainer.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public String getAllTextFromBodyContainer() {
		return bodyContainer.getText();

	}
}
