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

import static org.junit.Assert.assertEquals;

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
 * @author <a href="mailto:mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $
 */

public class MySpacePage extends BaseTest {

	// Locators basic elements For Top Menu
	private interface Locators {
		String CONTAINER_MESSAGE_APPEAR = "div.CommentBlockBoundNone";

		String ADD_NEW_SPACE_LINK = "Add New Space";

		String NAME_SPACE_PREFIX = "//div[@class='TitleContent']/a[text()='%s']";

		String CREATE_SPACE_BUTTON = "Create";

		String NAME_SPACE_FIELD_ID = "displayName";

		String FIRST_SPACE = "//div[@class='BoxSpaceList ClearFix']";

		String SECOND_SPACE = "//div[@class='GrayBox ClearFix FL']";

		String NAME_OF_FIRST_SPACE = FIRST_SPACE + NAME_SPACE_PREFIX;

		String NAME_OF_SECOND_SPACE = NAME_SPACE_PREFIX;

		String LINKS_OF_FIRST_SPASE = FIRST_SPACE
				+ "//ul[@class=\"ActionContent ClearFix\"]//li/a[text()='%s']";

		String LINKS_OF_SECOND_SPACE = SECOND_SPACE
				+ "//ul[@class=\"ActionContent ClearFix\"]//li/a[text()='%s']";

		String LINK_TO_EDIT_IN_FIRST_SPACE = FIRST_SPACE
				+ "//ul[@class=\"ActionContent ClearFix\"]//li/a[text()='%s']";

		String TAB_NAME_OF_CREATE_FORM = "//div[@class=\"MiddleTab\" and text()='%s']";

		// String OPEN_RADIO_BUTTON =
		// "//html/body/div/div[2]/div/div/div/div/div/div/div[2]/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div/table/tbody/tr/td/div/div/div/div/div/div/div/div/div/div/div/div/div[2]/div/div/div/div/div/form/div[3]/div/div[2]/div/div/div/table/tbody/tr[2]/td[2]/div/label";
		String OPEN_RADIO_BUTTON = "//table[@class='UIFormGrid']/tbody/tr[2]/td[2]//div/input[1]";

		String MENU_SPACES_LINKS = "//ul[@class='MenuSpace ClearFix']/li/a[text()='%s']";

		String SPACE_BY_NAME = "//div[@class=\"BoxSpaceList ClearFix\"]//a[contains(.,\"%s\")]";

		String SPACE_BUTTONS_BY_NAME = SPACE_BY_NAME
				+ "/following::ul[1]/li/a[contains(.,\"%s\")]";

	}

	@FindBy(linkText = Locators.ADD_NEW_SPACE_LINK)
	private WebElement addSpaceLink;

	@FindBy(id = Locators.NAME_SPACE_FIELD_ID)
	private WebElement nameSpaceField;

	@FindBy(linkText = Locators.CREATE_SPACE_BUTTON)
	private WebElement createBtn;

	@FindBy(xpath = Locators.CREATE_SPACE_BUTTON)
	private WebElement nameFirstWs;

	@FindBy(xpath = Locators.OPEN_RADIO_BUTTON)
	private WebElement openRadioBtn;

	@FindBy(css = Locators.CONTAINER_MESSAGE_APPEAR)
	private WebElement containerMessage;

	/**
	 * wait appearance basic elements from CW page
	 * 
	 * @throws Exception
	 */
	public void waitElementsAfterCreateWs() throws Exception {
		new WebDriverWait(driver, 300).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return containerMessage != null
						&& containerMessage.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance basic elements from MyWorkspace page
	 * 
	 * @throws Exception
	 */
	public void waitNewSpaceFormAppear() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return createBtn != null && createBtn.isDisplayed()
						&& createBtn != null && createBtn.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance radiobtn from Create New Workspace Form
	 * 
	 * @throws Exception
	 */
	public void waitOpenSpaseRadoiBtnAppear() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return openRadioBtn != null && openRadioBtn.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance link of creating WS
	 * 
	 * @throws Exception
	 */
	public void waitLinkinFirstSpaceAppear(final String nameOfFirstWs)
			throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement view = driver.findElement(By.xpath(String.format(
						Locators.LINKS_OF_FIRST_SPASE, nameOfFirstWs)));
				return view != null && view.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance link of creating WS
	 * 
	 * @throws Exception
	 */
	public void waitLinkinSecondSpaceAppear(final String nameOfSecondWs)
			throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement view = driver.findElement(By.xpath(String.format(
						Locators.LINKS_OF_SECOND_SPACE, nameOfSecondWs)));
				return view != null && view.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance WS with the specified name
	 * 
	 * @throws Exception
	 */
	public void waitFirstSpaceAppear(final String nameOfFirstWs)
			throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement view = driver.findElement(By.xpath(String.format(
						Locators.NAME_OF_FIRST_SPACE, nameOfFirstWs)));
				return view != null && view.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance WS with the specified name
	 * 
	 * @throws Exception
	 */
	public void waitSecondSpaceAppear(final String nameOfFirstWs)
			throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement view = driver.findElement(By.xpath(String.format(
						Locators.NAME_OF_SECOND_SPACE, nameOfFirstWs)));
				return view != null && view.isDisplayed();
			}
		});
	}

	/**
	 * wait while create workspace form is closed
	 * 
	 */
	public void waitNewSpaceFormDisAppear() throws Exception {
		new WebDriverWait(driver, 300).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement view = driver.findElement(By
							.linkText(Locators.CREATE_SPACE_BUTTON));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait appearance basic elements from MyWorkspace page
	 * 
	 * @throws Exception
	 */
	public void waitAddLink() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return addSpaceLink != null && addSpaceLink.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * type name to field
	 */
	public void typeSpaceName(String name) {
		nameSpaceField.sendKeys(name);
	}

	/**
	 * click on add new space button
	 */
	public void clickAddNewSpace() {
		addSpaceLink.click();
	}

	/**
	 * click on add new space button
	 */
	public void clickCreateBtn() {
		createBtn.click();
	}

	/**
	 * get name of first workspace
	 */
	public void getNameFirstWs() {
		nameFirstWs.getText();
	}

	/**
	 * click on tab create New space form
	 * 
	 * @param tabName
	 */
	public void clickOnTabCreateWSForm(String tabName) {
		WebElement tab = driver.findElement(By.xpath(String.format(
				Locators.TAB_NAME_OF_CREATE_FORM, tabName)));
		tab.click();
	}

	/**
	 * click for select open Ws
	 * 
	 * @param tabName
	 */
	public void clickOnOpenWsRadioBtn() {
		openRadioBtn.click();
	}

	/**
	 * click for select open Ws
	 * 
	 * @param tabName
	 */
	public void isRadiobtnOpenSelect() {
		openRadioBtn.isSelected();
	}

	/**
	 * click on link in first space with the specified name
	 * 
	 * @param name
	 */
	public void selectInPopUpMenuSpaces(String name) {
		driver.findElement(
				By.xpath(String.format(Locators.MENU_SPACES_LINKS, name)))
				.click();
	}

	/**
	 * click on space buttons edit, leave, delete
	 */
	public void clickOnSpaceButton(String spaceName, String buttonName) {
		driver.findElement(
				By.xpath(String.format(Locators.SPACE_BUTTONS_BY_NAME,
						spaceName, buttonName))).click();
	}

	/**
	 * check that space button is disappeared
	 */
	public void checkThatSpaceButtonIsDisappeared(final String spaceName,
			final String buttonName) {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver button) {
				try {
					button.findElement(By.xpath(String.format(
							Locators.SPACE_BUTTONS_BY_NAME, spaceName,
							buttonName)));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	/**
	 * check for space buttons
	 */
	public void checkThatSpaceButtonIsAppeared(final String spaceName,
			final String buttonName) {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver button) {
				try {
					return button.findElement(
							By.xpath(String.format(
									Locators.SPACE_BUTTONS_BY_NAME, spaceName,
									buttonName))).isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * click on space name to enter sapce
	 */
	public void clickOnSpaceNameToEnterSpace(String spaceName) {
		driver.findElement(
				By.xpath(String.format(Locators.SPACE_BY_NAME, spaceName)))
				.click();
	}

	/**
	 * verify that space is deleted
	 */
	public void verifyThatSpaceDeleted(final String spaceName) throws Exception {
		new WebDriverWait(driver, 120).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				try {
					input.findElement(By.xpath(String.format(
							Locators.SPACE_BY_NAME, spaceName)));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	/**
	 * Creates open space with specified name
	 * 
	 * @param spaceName
	 * @throws Exception
	 */
	public void createOpenSpace(String spaceName) throws Exception {
		CW.TOPMENUS.waitLink("Browse");
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.clickAddNewSpace();
		CW.SPACES.waitNewSpaceFormAppear();
		CW.SPACES.typeSpaceName(spaceName);
		CW.SPACES.clickOnTabCreateWSForm("Access & Edit");
		Thread.sleep(3000);
		CW.SPACES.waitOpenSpaseRadoiBtnAppear();
		CW.SPACES.clickOnOpenWsRadioBtn();
		// need for select radiobtn
		Thread.sleep(3000);
		CW.SPACES.isRadiobtnOpenSelect();
		// need for appearance createBtn
		Thread.sleep(3000);
		CW.SPACES.clickCreateBtn();
		CW.SPACES.waitNewSpaceFormDisAppear();
		CW.SPACES.waitElementsAfterCreateWs();
		assertEquals(CW.WORKSPACE.getTitlemess(1), spaceName);
	}

	/**
	 * Creates space with validation and specified name
	 * 
	 * @param spaceName
	 * @throws Exception
	 */
	public void createSpaceWithValidation(String spaceName) throws Exception {
		CW.TOPMENUS.waitLink("Browse");
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.clickAddNewSpace();
		CW.SPACES.waitNewSpaceFormAppear();
		CW.SPACES.typeSpaceName(spaceName);
		CW.SPACES.clickCreateBtn();
		CW.SPACES.waitNewSpaceFormDisAppear();
		CW.SPACES.waitElementsAfterCreateWs();
		assertEquals(CW.WORKSPACE.getTitlemess(1), spaceName);
	}

	/**
	 * Deletes space by name
	 * 
	 * @param spaceName
	 * @throws Exception
	 */
	public void deleteSpace(String spaceName) throws Exception {
		CW.TOPMENUS.waitLink("Browse");
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(spaceName, "Delete");
		CW.SPACES.clickOnSpaceButton(spaceName, "Delete");
		driver.switchTo().alert().accept();
		CW.SPACES.verifyThatSpaceDeleted(spaceName);
	}

}
