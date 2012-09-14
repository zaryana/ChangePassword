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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:riuvshin@exoplatform.com">Iuvshin Roman</a>
 * @version $
 */
public class SpaceSettingsPage extends BaseTest {

	private interface Locators {
		String SPACE_SETTINGS = "//*[@id=\"UISpaceSetting\"]";

		String SETTINGS_BUTTON = "//*[@class=\"UIHorizontalTabs NewTabs\"]/*//div[@class=\"MiddleTab\" and contains(.,\"%s\")]";

		String ADD_MEMBER_FORM = "//*[@id=\"UISpaceMember\"]";

		String VALIDATE_INVITATION_BUTTON = "//a[@title=\"Validate Invitation\"]/span[@class=\"ValidateButton\"]";

		String GRANT_MANAGER_BUTTON = "//a[@title=\"Grant Manager\"]/span[@class=\"ManageMemButtonAdd label\"]";
	}

	@FindBy(xpath = Locators.SPACE_SETTINGS)
	private WebElement spaceSettings;

	@FindBy(xpath = Locators.SETTINGS_BUTTON)
	private WebElement settingsButton;

	@FindBy(xpath = Locators.ADD_MEMBER_FORM)
	private WebElement addMemberForm;

	@FindBy(xpath = Locators.VALIDATE_INVITATION_BUTTON)
	private WebElement validateInvitationButton;

	@FindBy(xpath = Locators.GRANT_MANAGER_BUTTON)
	private WebElement grantManagerButton;

	/**
	 * wait appearance settings menu
	 * 
	 * @throws Exception
	 */
	public void waitSettingsMenu() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return spaceSettings != null && spaceSettings.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait appearance add member form
	 * 
	 * @throws Exception
	 */
	public void waitAddMemberForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return addMemberForm != null && addMemberForm.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait appearance validate invitation button
	 * 
	 * @throws Exception
	 */
	public void waitValidateInvitationButton() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return validateInvitationButton != null
							&& validateInvitationButton.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * verify that validate invitation button is disappeared
	 * 
	 * @throws Exception
	 */
	public void waitValidateInvitationButtonDisappear() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver button) {
				try {
					button.findElement(By
							.xpath(Locators.VALIDATE_INVITATION_BUTTON));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait appearance validate invitation button
	 * 
	 * @throws Exception
	 */
	public void wainGrantManagerButton() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return grantManagerButton != null
							&& grantManagerButton.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * click on needed button in space settings
	 * 
	 * @param buttonName
	 */
	public void clickOnSpecifiedSettingsButton(String buttonName) {
		WebElement tab = driver.findElement(By.xpath(String.format(
				Locators.SETTINGS_BUTTON, buttonName)));
		tab.click();
	}

	/**
	 * click on validate invitation button
	 */
	public void clickOnValidateInvitationButton() {
		validateInvitationButton.click();
	}

}
