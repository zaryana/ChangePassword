/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package com.exoplatform.cw.core;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $
 */

public class SignUpPage extends BaseTest {

	// Locators basic elements Dialog About Menu
	private interface Locators {
		String UI_FORM = "UIForm";

		String SIGN_INFO = "SignUpInfo";

		String INPUT_ID = "email";

		String SIGN_UP_BUTTON = "t_submit";

		String SIGN_IN_ALREDY_REGISTERED_LINK = "Already registered? Sign In";

		String INFO = "Cloud Workspaces is currently in private beta. To participate, register with your work email address. We'll keep you updated when your company's social intranet is ready.";
	}

	// WebElemnts DialogAbout menu
	@FindBy(id = Locators.INPUT_ID)
	private WebElement input;

	@FindBy(className = Locators.SIGN_INFO)
	private WebElement signUpInfo;

	@FindBy(id = Locators.SIGN_UP_BUTTON)
	private WebElement signUpBtn;

	@FindBy(linkText = Locators.SIGN_IN_ALREDY_REGISTERED_LINK)
	private WebElement linksOnSignForm;

	@FindBy(className = Locators.UI_FORM)
	private WebElement signForm;

	// wait appear sign form
	/**
	 * wait base elements of sign form
	 * 
	 * @throws Exception
	 */
	public void waitSignForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return input != null && input.isDisplayed()
							&& signUpBtn != null && signUpBtn.isDisplayed()
							&& signForm != null && signForm.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * Type info to email field
	 * 
	 * @param mail
	 */
	public void typeAdress(String mail) {
		input.sendKeys(mail);
	}

	/**
	 * clear email field
	 * 
	 */
	public void clearField() {
		input.clear();
	}

	/**
	 * clear email field
	 * 
	 */
	public String getTextField() {
		return input.getText();

	}

	/**
	 * click on sighn up button
	 */
	public void signBtnClick() {
		signUpBtn.click();
	}

}
