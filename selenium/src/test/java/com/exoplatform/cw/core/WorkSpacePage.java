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

public class WorkSpacePage extends BaseTest {

	// Locators basic elements Dialog About Menu
	private interface Locators {

		String NUM_MESSAGES = "//div[@id=\"UIActivitiesLoader\"]//div[@class=\"UIActivity\"][%s]";

		String TITLE_MESSAGE = NUM_MESSAGES + "//h5";

		String POST_MESSAGES = NUM_MESSAGES + "//div[@class=\"Content\"]";

		String STATUS_FIELD = "composerInput";

		String SHARE_BUTTON = "ShareButton";

		String COMMENT_LINK = NUM_MESSAGES
				+ "//div/a[@class and contains(text(), \"Comment\" )]";

		String LIKE_LINK = NUM_MESSAGES
				+ "//div/a[@id and contains(text(), \"Like\" )]";

		String UNLIKE_LINK = NUM_MESSAGES
				+ "//div/a[@id and contains(text(), \"Unlike\" )]";

		String COMMENT_MESSAGE_CLASS = NUM_MESSAGES
				+ "//div[@class=\"CommentBlockBound\"]";

		String GET_COMMENT_CLASS = COMMENT_MESSAGE_CLASS
				+ "//p[@class=\"ContentBlock\"]";

		String COMMENT_BOX_CLASS = COMMENT_MESSAGE_CLASS
				+ "//div[@class=\"CommentInputBox\"]/textarea";

		String COMMENT_BUTTON_CLASS = "//input[@class=\"CommentButton DisplayNone\"]";

		String LIKE_APPERENCE_CLASS = NUM_MESSAGES
				+ "//div[@class=\"ListPeopleContent\"]";

		String EDIT_PROFILE_LINK = "Edit My Profile";

		String CALENDAR_LINK = "Calendar";

		String MY_PROFILE_IFRAME_ID = "remote_iframe_0";

		String CALENDAR_IFRAME_ID = "remote_iframe_1";

		String LINKS_ON_MAIN_PAGE = "%s";

		String DELETE_COMMENT_BTN = NUM_MESSAGES
				+ "//span[contains(@id, \"DeleteCommentButton\")]";

		String DELETE_STATUS_BTN = NUM_MESSAGES
				+ "//span[contains(@id, \"DeleteActivityButton\")]";

		String CHECK_TEXT_IN_FIRST_STATUS_BOX = "//div[@id=\"UIActivitiesLoader\"]//div[@class=\"UIActivity\"][1]//*[contains(text(), '%s' )]";

		String HOME_LINK = "//a[@class=\"HomeLink\"]";

		String TOP_MENU_BY_NAMES = "//ul[@id=\"PortalNavigationTopContainer\"]//a[contains(.,\"%s\")]";

		String TOP_MENU_ITEM_BY_NAME = "//ul[@class=\"MenuItemContainer\"]//a[contains(.,\"%s\")]";

	}

	// basic page elements
	@FindBy(id = Locators.STATUS_FIELD)
	private WebElement statusField;

	@FindBy(id = Locators.SHARE_BUTTON)
	private WebElement shareBtn;

	@FindBy(xpath = Locators.LIKE_LINK)
	private WebElement likeLink;

	@FindBy(xpath = Locators.UNLIKE_LINK)
	private WebElement unLikeLink;

	@FindBy(className = Locators.COMMENT_BOX_CLASS)
	private WebElement commentBox;

	@FindBy(linkText = Locators.EDIT_PROFILE_LINK)
	private WebElement editProfile;

	@FindBy(xpath = Locators.TITLE_MESSAGE)
	private WebElement title;

	@FindBy(partialLinkText = Locators.CALENDAR_LINK)
	private WebElement calendar;

	@FindBy(id = Locators.CALENDAR_IFRAME_ID)
	private WebElement calendIframe;

	@FindBy(xpath = Locators.DELETE_STATUS_BTN)
	private WebElement deleteStatusBnt;

	@FindBy(xpath = Locators.DELETE_COMMENT_BTN)
	private WebElement deleteCommentBtn;

	@FindBy(xpath = Locators.HOME_LINK)
	private WebElement homeLink;

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance like message from CW page num start with 1
	 */
	public void waitLikeAppear(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement comment = driver.findElement(By.xpath(String
							.format(Locators.LIKE_APPERENCE_CLASS, numMess)));
					return comment != null && comment.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public void verifyThatUnlikeDisappear(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver unlike) {
				try {
					unlike.findElement(By.xpath(String.format(
							Locators.UNLIKE_LINK, numMess)));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait link with unique name from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitNewLinkAppear(final String link) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.linkText(String.format(
						Locators.LINKS_ON_MAIN_PAGE, link)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance comment link from CW page num start with 1
	 */
	public void waitCommentLinkAppear(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement comment = driver.findElement(By.xpath(String
							.format(Locators.COMMENT_LINK, numMess)));
					return comment != null && comment.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance message from CW page num start with 1
	 */
	public void waitPostComment(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement comment = driver.findElement(By.xpath(String
							.format(Locators.COMMENT_MESSAGE_CLASS, numMess)));
					return comment != null && comment.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance message from CW page num start with 1
	 */
	public void waitSendCommentBtn(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.NUM_MESSAGES
									+ Locators.COMMENT_BUTTON_CLASS, numMess)));
					return mess != null && mess.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance calendar link on main page num start with 1
	 */
	public void waitCalendarLink() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return calendar != null && calendar.isDisplayed();
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance message from CW page num start with 1
	 */
	public void waitMessage(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.NUM_MESSAGES, numMess)));
					return mess != null && mess.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait comment-textarea in message
	 * 
	 * @param numMess
	 * @param numComment
	 * @throws Exception
	 */
	public void waitCommentBox(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.COMMENT_BOX_CLASS, numMess)));
					return mess != null && mess.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance comment-message from CW page num start with 1
	 */
	public void waitCommentMessage(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.COMMENT_MESSAGE_CLASS, numMess)));
					return mess != null && mess.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public void switchToCW() {
		driver.switchTo().window("Home Page");
	}

	/**
	 * wait appearance basic elements from CW page
	 * 
	 * @throws Exception
	 */
	public void waitBasicElements() throws Exception {
		new WebDriverWait(driver, 120).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return statusField != null && statusField.isDisplayed()
							&& shareBtn != null && shareBtn.isDisplayed()
							&& calendIframe != null
							&& calendIframe.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * wait appearance basic elements from CW page
	 * 
	 * @throws Exception
	 */
	public void waitCalendarIframe() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return calendIframe != null && calendIframe.isDisplayed();
			}

		});
	}

	/**
	 * @param numMess
	 * @throws Exception
	 *             wait appearance comment-message from CW page num start with 1
	 */
	public void waitMessageAfterCommClick(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.GET_COMMENT_CLASS, numMess)));
					return mess != null && mess.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	/**
	 * Type messedges to status fuiel in page
	 * 
	 * @param msg
	 */
	public void typeToStatusField(String msg) {
		statusField.clear();
		statusField.click();
		statusField.sendKeys(msg);
	}

	/**
	 * get text in statusfield
	 * 
	 * @return
	 */
	public String geTextStatusField() {
		return statusField.getText();
	}

	/**
	 * get text from poster star with 1
	 * 
	 * @param numMess
	 * @return
	 */
	public String getMessage(int numMess) {
		WebElement post = driver.findElement(By.xpath(String.format(
				Locators.POST_MESSAGES, numMess)));
		return post.getText();
	}

	/**
	 * click on share button
	 */
	public void clickShareBtn() {
		shareBtn.click();
	}

	/**
	 * click on share button
	 */
	public void clickComment(int num) {
		WebElement lnk = driver.findElement(By.xpath(String.format(
				Locators.COMMENT_LINK, num)));
		new Actions(driver).moveToElement(lnk).build().perform();
		lnk.click();
	}

	/**
	 * type cooment to texr area
	 */
	public void typeComment(int numMess, String comment) {
		WebElement mess = driver.findElement(By.xpath(String.format(
				Locators.COMMENT_BOX_CLASS, numMess)));
		mess.sendKeys(comment);

	}

	/**
	 * type cooment to texr area
	 */
	public void sendComment(int numMess) {
		WebElement mess = driver
				.findElement(By.xpath(String.format(Locators.NUM_MESSAGES
						+ Locators.COMMENT_BUTTON_CLASS, numMess)));
		mess.click();
	}

	/**
	 * @param comment
	 * @return
	 */
	public String getComment(int numMess) {
		WebElement comment = driver.findElement(By.xpath(String.format(
				Locators.GET_COMMENT_CLASS, numMess)));
		return comment.getText();
	}

	/**
	 * @param numMess
	 * @return
	 */
	public void clickOnLikeLink(int numMess) {
		WebElement like = driver.findElement(By.xpath(String.format(
				Locators.LIKE_LINK, numMess)));
		like.click();
	}

	public void clickOnUnlikeLink(int numMess) {
		WebElement unlike = driver.findElement(By.xpath(String.format(
				Locators.UNLIKE_LINK, numMess)));
		unlike.click();
	}

	/**
	 * @param numMess
	 * @return
	 */
	public void selectMyProfileIframe() {

		driver.switchTo().frame(Locators.MY_PROFILE_IFRAME_ID);

	}

	/**
	 * @param numMess
	 * @return
	 */
	public void clickOnMyProfileLink() {
		editProfile.click();
		selectMainFrame();
	}

	/**
	 * @param numMess
	 * @return
	 */
	public String getTitlemess(int numMessage) {
		WebElement title = driver.findElement(By.xpath(String.format(
				Locators.TITLE_MESSAGE, numMessage)));
		return title.getText();

	}

	/**
	 * Click on calendar in Applications
	 * 
	 * @throws Exception
	 */
	public void clickOnCalendar() throws Exception {
		waitCalendarIframe();
		driver.switchTo().frame(Locators.CALENDAR_IFRAME_ID);
		waitCalendarLink();
		calendar.click();
		selectMainFrame();
	}

	public void waitForDeleteStatusBtn(final int numMess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement mess = driver.findElement(By.xpath(String
							.format(Locators.DELETE_STATUS_BTN, numMess)));
					return mess != null && mess.isDisplayed();

				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public void verifyThatStatusDeleted(final String text) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				try {
					input.findElement(By.xpath(String.format(
							Locators.CHECK_TEXT_IN_FIRST_STATUS_BOX, text)));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	public void verifyThatCommentDeleted(final String text) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver input) {
				try {
					input.findElement(By.xpath(String.format(
							Locators.CHECK_TEXT_IN_FIRST_STATUS_BOX, text)));
					return false;
				} catch (NoSuchElementException e) {
					return true;
				}
			}
		});
	}

	public void deleteStatusBtn(int num) {
		WebElement lnk = driver.findElement(By.xpath(String.format(
				Locators.DELETE_STATUS_BTN, num)));
		new Actions(driver).moveToElement(lnk).build().perform();
		lnk.click();
	}

	public void deleteCommentBtn(int num) {
		WebElement lnk = driver.findElement(By.xpath(String.format(
				Locators.DELETE_COMMENT_BTN, num)));
		new Actions(driver).moveToElement(lnk).build().perform();
		lnk.click();
	}

	public void switchToPage() {
		driver.getWindowHandle();
	}

	/**
	 * Home button
	 */
	public void goToHome() {
		homeLink.click();
	}

	/**
	 * Deletes status message
	 */
	public void deleteStatusMessage(int messageNumber, String statusMessage)
			throws Exception {
		CW.WORKSPACE.deleteStatusBtn(messageNumber);
		driver.switchTo().alert().accept();
		CW.WORKSPACE.verifyThatStatusDeleted(statusMessage);
	}

	/**
	 * Post status message
	 */
	public void postStatusMessage(String message) throws Exception {
		CW.WORKSPACE.typeToStatusField(message);
		CW.WORKSPACE.clickShareBtn();
		CW.WORKSPACE.waitMessage(1);
		Thread.sleep(5000);
		assertEquals(message, CW.WORKSPACE.getMessage(1));
	}

	/**
	 * move mouse to Apps menu
	 */
	public void moveMouseToAppsMenu(String menuName) {
		WebElement apps = driver.findElement(By.xpath(String.format(
				Locators.TOP_MENU_BY_NAMES, menuName)));
		new Actions(driver).moveToElement(apps).build().perform();

	}

	/**
	 * wait appearance menu item with name
	 * 
	 * @param menuName
	 * @throws Exception
	 */
	public void waitMenuItem(final String menuName) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.xpath(String.format(
						Locators.TOP_MENU_ITEM_BY_NAME, menuName)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * click on top menu item with name
	 * 
	 * @param menuName
	 */
	public void clickOnMenuItem(String menuName) {
		WebElement lnk = driver.findElement(By.xpath(String.format(
				Locators.TOP_MENU_ITEM_BY_NAME, menuName)));
		new Actions(driver).moveToElement(lnk).build().perform();
		lnk.click();
	}

}
