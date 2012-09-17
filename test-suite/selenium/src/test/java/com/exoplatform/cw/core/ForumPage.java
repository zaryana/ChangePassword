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

public class ForumPage extends BaseTest {

	// Locators basic elements Dialog About Menu
	private interface Locators {
		String FIRST_TOPIC_BUTTON = "div.StartTopicForum FL>a";

		String LAST_ELEMENT_ON_PAGE = "//div[@class='IconForum ThreadNoNewClosePost' and text()='Topic is closed']";

		String TOPIC_SELECTION = "//div[@class='ButtomAndPageListContainer ClearFix'][%s]//a";

		String TITLE_FIELD_ID = "ThreadTitle";

		String TITLE_FIELD_POSTFORM_ID = "PostTitle";

		String IFRAME_MESSAGE_TOPIC_ID = "messageContent___Frame";

		String IFRAME_MESSAGE_POST_ID = "MessageContent___Frame";

		String IFRAME_EDITABLE_MESSAGE_TOPIC_CSS = "iframe[frameborder='0']";

		String MESSAGE_FIELD = "p";

		String MESSAGE_TOPIC_CONTAINER = "xEditingArea";

		String LAST_CONTAINER_NEW_TOPIC_FORM_ID = "UITopicForm";

		String LAST_CONTAINER_NEW_POST_FORM_ID = "UIPostForm";

		String SUBMIT_BTN = "Submit";

		String LINKS_ON_FORUM_PAGE = "%s";

		String LAST_ELEMENT_ON_TOPIC_PAGE_ID = "UIPostRules";

		String POST_MASSAGE_CONTAINER = "//table[@class='TablePost ContentContainer'][%s]//div/p";

		String FOOTER_POST_MASSAGE_CONTAINER = "//table[@class='TablePost ContentContainer'][%s]";

		String BUTTON_POST_MASSAGE_CONTAINER = FOOTER_POST_MASSAGE_CONTAINER
				+ "//a[text()='%s']";

		String UPLOAD_FORM_ID = "UIAttachFileForm";

		String UPLOAD_FORM_FIRST_FIELD = "div.UploadInput>input";

		String FILE_NAME_LABEL = "//div[@class='FileNameLabel' and text()=' %s']";

		String IMAGE_ALT_ATTRIBUTE_CLASS = "img[alt='%s']";

		String LINK_TITLE_CLASS = "a[title='%s']";

		String SEARCH_INPUT_ID = "inputValue";

		String SEARCH_BTN = "a[title=Search]";

		String POST_CONTENT = "//div[@class=\"PostContent\"]//*[contains(.,\"%s\")]";

		String QUICK_REPLY_TEXT_AREA_ID = "Message";

	}

	// basic page elements
	@FindBy(linkText = Locators.FIRST_TOPIC_BUTTON)
	private WebElement startFirstTopic;

	@FindBy(id = Locators.TITLE_FIELD_ID)
	private WebElement titleField;

	@FindBy(id = Locators.IFRAME_MESSAGE_TOPIC_ID)
	private WebElement iframeMessageField;

	@FindBy(id = Locators.LAST_CONTAINER_NEW_TOPIC_FORM_ID)
	private WebElement lastTopicFormContainer;

	@FindBy(linkText = Locators.SUBMIT_BTN)
	private WebElement submitToicBtn;

	@FindBy(css = Locators.IFRAME_EDITABLE_MESSAGE_TOPIC_CSS)
	private WebElement editableMessageIframe;

	@FindBy(tagName = Locators.MESSAGE_FIELD)
	private WebElement messageField;

	@FindBy(id = Locators.LAST_ELEMENT_ON_TOPIC_PAGE_ID)
	private WebElement lastTopicPage;

	@FindBy(id = Locators.LAST_CONTAINER_NEW_POST_FORM_ID)
	private WebElement postForm;

	@FindBy(id = Locators.TITLE_FIELD_POSTFORM_ID)
	private WebElement postField;

	@FindBy(id = Locators.IFRAME_MESSAGE_POST_ID)
	private WebElement postIframe;

	@FindBy(css = Locators.UPLOAD_FORM_FIRST_FIELD)
	private WebElement uploadField;

	@FindBy(xpath = Locators.BUTTON_POST_MASSAGE_CONTAINER)
	private WebElement buttonMessCont;

	@FindBy(id = Locators.UPLOAD_FORM_ID)
	private WebElement uploadForm;

	@FindBy(id = Locators.SEARCH_INPUT_ID)
	private WebElement searchField;

	@FindBy(css = Locators.SEARCH_BTN)
	private WebElement searchBtn;

	@FindBy(id = Locators.QUICK_REPLY_TEXT_AREA_ID)
	private WebElement quickReplyTextArea;

	public void starttFirstTopic() throws Exception {
		startFirstTopic.click();
	}

	/**
	 * wait appearance basic elements from Forum page
	 * 
	 * @throws Exception
	 */
	public void waitOpeningForum() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By
						.xpath(Locators.LAST_ELEMENT_ON_PAGE));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * wait upload form for attach file
	 * 
	 * @throws Exception
	 */
	public void waitFileNameOnUploadForm(final String fileName)
			throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement fl = driver.findElement(By.xpath(String.format(
						Locators.FILE_NAME_LABEL, fileName)));
				return fl != null && fl.isDisplayed();
			}
		});
	}

	/**
	 * wait upload form for attach file
	 * 
	 * @throws Exception
	 */
	public void waitUploadForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return uploadForm != null && uploadForm.isDisplayed();
			}
		});
	}

	/**
	 * wait image the specified name attachment
	 * 
	 * @param img
	 * @throws Exception
	 */
	public void waitAttacmentImage(final String img) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement image = driver.findElement(By.cssSelector(String
						.format(Locators.IMAGE_ALT_ATTRIBUTE_CLASS, img)));
				return image != null && image.isDisplayed();
			}
		});
	}

	/**
	 * wait title with the specified name attachment img
	 * 
	 * @param img
	 * @throws Exception
	 */
	public void waitAttacmentTitleImage(final String tit) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement title = driver.findElement(By.cssSelector(String
						.format(Locators.LINK_TITLE_CLASS, tit)));
				return title != null && title.isDisplayed();

			}
		});
	}

	/**
	 * wait upload form for attach file
	 * 
	 * @throws Exception
	 */
	public void waitCloseUploadForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement visible = driver.findElement(By
							.id(Locators.UPLOAD_FORM_ID));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait last container class from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitTopicForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return lastTopicFormContainer != null
						&& lastTopicFormContainer.isDisplayed();
			}
		});
	}

	/**
	 * wait last topic page
	 * 
	 * @throws Exception
	 */
	public void waitPostForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return postForm != null && postForm.isDisplayed();
			}
		});
	}

	/**
	 * wait last topic page
	 * 
	 * @throws Exception
	 */
	public void waitPostFormClosed() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {

				try {
					WebElement element = driver.findElement(By
							.id(Locators.LAST_CONTAINER_NEW_POST_FORM_ID));
					return false;
				} catch (Exception e) {
					return true;
				}

			}
		});
	}

	/**
	 * wait last topic page
	 * 
	 * @throws Exception
	 */
	public void waitTopicPage() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return lastTopicPage != null && lastTopicPage.isDisplayed();
			}
		});
	}

	/**
	 * wait last container class from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitTopicFormClosed() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement visible = driver.findElement(By
							.className(Locators.LAST_CONTAINER_NEW_TOPIC_FORM_ID));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait post messages on discution forum start with first mess
	 * 
	 * @param mess
	 * @throws Exception
	 */
	public void waitPostDiscutionMess(final int mess) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement messageContainer = driver.findElement(By
						.xpath(String.format(
								Locators.FOOTER_POST_MASSAGE_CONTAINER, mess)));
				return messageContainer != null
						&& messageContainer.isDisplayed();
			}
		});
	}

	/**
	 * wait last container class from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitNewLinkAppearWithPrefix(final String link) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.partialLinkText(String
						.format(Locators.LINKS_ON_FORUM_PAGE, link)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * wait last container class from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitMessageIframe() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return editableMessageIframe != null
						&& editableMessageIframe.isDisplayed();
			}
		});
	}

	/**
	 * wait last container class from Topic form
	 * 
	 * @throws Exception
	 */
	public void waitNewLinkAppear(final String link) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.linkText(String.format(
						Locators.LINKS_ON_FORUM_PAGE, link)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * click on topic button run with 1
	 * 
	 * @param numTopic
	 * @throws Exception
	 */
	public void clickOnStartTopic(int numTopic) throws Exception {
		WebElement topic = driver.findElement(By.xpath(String.format(
				Locators.TOPIC_SELECTION, numTopic)));
		topic.click();
	}

	/**
	 * click on topic button run with 1
	 * 
	 * @param numTopic
	 * @throws Exception
	 */
	public void typeTitleMess(String TitleMess) throws Exception {
		titleField.clear();
		titleField.sendKeys(TitleMess);
	}

	/**
	 * type to title field post form
	 */
	public void typePostFormMess(String TitleMess) throws Exception {
		postField.clear();
		postField.sendKeys(TitleMess);
	}

	/**
	 * type to New message form
	 * 
	 * @param mess
	 * @throws Exception
	 */
	public void typeMess(String mess) throws Exception {
		Thread.sleep(3000);
		driver.switchTo().frame(iframeMessageField);
		Thread.sleep(3000);
		driver.switchTo().frame(editableMessageIframe);
		Thread.sleep(3000);
		messageField.sendKeys(mess);
		Thread.sleep(3000);
		selectMainFrame();
	}

	/**
	 * type to New message to post form
	 * 
	 * @param mess
	 * @throws Exception
	 */
	public void typePostMess(String mess) throws Exception {
		driver.switchTo().frame(postIframe);
		waitMessageIframe();
		driver.switchTo().frame(editableMessageIframe);
		Thread.sleep(2000);
		messageField.sendKeys(mess);
		Thread.sleep(2000);
		selectMainFrame();
	}

	/**
	 * click on submit topic button
	 */
	public void clickOnSubmitTopicForm() {
		submitToicBtn.click();
	}

	/**
	 * click on first find link with the specified name in DOM forum page
	 * 
	 * @param link
	 */
	public void clickOnLinkForumPage(String link) {
		driver.findElement(
				By.linkText(String.format(Locators.LINKS_ON_FORUM_PAGE, link)))
				.click();

	}

	/**
	 * return text from disscutions messages start with 1
	 * 
	 * @param numMess
	 * @return
	 */
	public String getTextDisscution(int numMess) {
		WebElement mess = driver.findElement(By.xpath(String.format(
				Locators.POST_MASSAGE_CONTAINER, numMess)));
		return mess.getText();
	}

	/**
	 * click on message-button with the specified name
	 * 
	 * @param numMess
	 * @param nameBtn
	 * @return
	 */
	public void clickOnButtonInPostMess(int numMess, String nameBtn) {
		WebElement mess = driver.findElement(By.xpath(String.format(
				Locators.BUTTON_POST_MASSAGE_CONTAINER, numMess, nameBtn)));
		mess.click();
	}

	/**
	 * for typing path to attached files
	 *
	 * @param path
	 */
	public void typeToUplodField(String path) {
		uploadField.sendKeys(path);
	}

	/**
	 * for typing for search on disscutions page
	 *
	 * @param text
	 */
	public void typeToSearch(String text) {
		searchField.clear();
		searchField.sendKeys(text);
	}

	/**
	 * click on search icon in disscution page
	 * 
	 * @param numMess
	 * @param nameBtn
	 */
	public void clickSerch() {
		searchBtn.click();
	}

	/**
	 * click on link click on link with unique name prefix
	 */
	public void clickOnLinkWithNamePrefixName(String link) {
		driver.findElement(
				By.partialLinkText(String.format(Locators.LINKS_ON_FORUM_PAGE,
						link))).click();
	}

	/**
	 * Check post content
	 * 
	 * @throws Exception
	 */
	public void checkPostContent(final String content) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement message = driver.findElement(By.xpath(String.format(
						Locators.POST_CONTENT, content)));
				return message != null && message.isDisplayed();
			}
		});
	}

	/**
	 * Typing quick reply message
	 * 
	 * @param message
	 */
	public void typeMessageInQuckReplyTextArea(String message) {
		quickReplyTextArea.sendKeys(message);
	}
}
