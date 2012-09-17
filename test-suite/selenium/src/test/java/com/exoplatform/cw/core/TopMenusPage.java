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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

public class TopMenusPage extends BaseTest {

	// Locators basic elements For Top Menu
	private interface Locators {
		String INTRANETLINK = "ul#PortalNavigationTopContainer>li>a";

		String MY_SPACES_LINK = "Browse";

		String HOME_ICON_LINK = "HomeLink";

		String MY_SPACES_MENU_LINK = "My Spaces";

		String USER_NAVIGATION_LINK = "li#UserNavigationTabsContainer>a";

		String LOGOUTUSER_LINK = "Logout";

		String SUBLINK = "%s";

	}

	@FindBy(className = Locators.HOME_ICON_LINK)
	private WebElement goToHomeIcon;

	@FindBy(linkText = Locators.MY_SPACES_MENU_LINK)
	private WebElement mySpacesTopMenu;

	@FindBy(css = Locators.USER_NAVIGATION_LINK)
	private WebElement userNavigation;

	@FindBy(linkText = Locators.LOGOUTUSER_LINK)
	private WebElement logout;

	@FindBy(css = Locators.INTRANETLINK)
	private WebElement intranetLink;

	@FindBy(linkText = Locators.MY_SPACES_LINK)
	private WebElement mySpacesLink;

	/**
	 * click on home icon on tom menus
	 */
	public void clickMySpacesMenu() {
		mySpacesTopMenu.click();
	}

	/**
	 * click on hom icon on tom menus
	 */
	public void clickOnGoToHome() {
		goToHomeIcon.click();
	}

	/**
	 * click on hom icon on tom menus
	 */
	public void mooveMouseToUserForm() {
		new Actions(driver).moveToElement(userNavigation).build().perform();

	}

	/**
	 * click for logout
	 */
	public void logoutUser() {
		logout.click();
	}

	/**
	 * Move mouse to navigation menu
	 * 
	 * @param topMenu
	 */
	public void mooveMouseToMySpacesMenu() {
		new Actions(driver).moveToElement(mySpacesLink).build().perform();
	}

	/**
	 * Move mouse to navigation menu
	 * 
	 * @param topMenu
	 */
	public void mooveMouseToIntranetMenu() {
		new Actions(driver).moveToElement(intranetLink).build().perform();
	}

	/**
	 * wait appearance basic elements from CW page
	 * 
	 * @throws Exception
	 */
	public void waitSubLink(final String subLnk) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.partialLinkText(String
						.format(Locators.SUBLINK, subLnk)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * wait link with text on page from CW page
	 * 
	 * @throws Exception
	 */
	public void waitLink(final String subLnk) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.partialLinkText(String
						.format(Locators.SUBLINK, subLnk)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * Move mouse to sublink menu
	 * 
	 * @param topMenu
	 * @throws Exception
	 */
	public void moveMouseToTopSubMenu(String topMenu) throws Exception {
		waitSubLink(topMenu);
		WebElement sub = driver.findElement(By.partialLinkText(String.format(
				Locators.SUBLINK, topMenu)));
		new Actions(driver).moveToElement(sub).build().perform();
	}

	/**
	 * Move mouse to sublink menu
	 * 
	 * @param topMenu
	 * @throws Exception
	 */
	public void clickOnSublink(String topMenu) throws Exception {
		waitSubLink(topMenu);
		WebElement sub = driver.findElement(By.linkText(String.format(
				Locators.SUBLINK, topMenu)));
		sub.click();
	}

	/**
	 * Move mouse to sublink menu
	 * 
	 * @param topMenu
	 * @throws Exception
	 */
	public void clickOnLink(String topMenu) throws Exception {
		waitSubLink(topMenu);
		WebElement sub = driver.findElement(By.partialLinkText(String.format(
				Locators.SUBLINK, topMenu)));
		sub.click();
	}
	
	/**
	 * wait user menu
	 */
	public void waitUserMenu(){
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
		public Boolean apply(WebDriver elem) {
					return userNavigation != null && userNavigation.isDisplayed();
		}
	});	
}
	/**
	 * wait logout button
	 */
	public void waitUserMenuLogoutButton(){
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
		public Boolean apply(WebDriver elem) {
					return logout != null && logout.isDisplayed();
		}
	});	
}
}
