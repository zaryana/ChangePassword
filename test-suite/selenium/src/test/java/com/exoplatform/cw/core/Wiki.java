package com.exoplatform.cw.core;

import static org.junit.Assert.assertEquals;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

public class Wiki extends BaseTest {
	private interface Locators {
		String MORE_MENU_ID = "UIWikiPageControlArea_PageToolBar_More_";

		String UNIQUE_LINK_NAME = "%s";

		String EDIT_BTN = "a[title=Edit]";

		String LEFT_AREA_WIKIHOME = "UILeftContainerArea";

		String CENTRAL_AREA_WIKIHOME = "UIWikiPageArea";

		String TITLE_ID_WIKIHOME_DOC = "TitleInfo";

		String CONTENT_WIKIHOME_DOC_CLASS = "WikiContent";

		String ADD_PAGE_MENU_ID = "UIWikiPageControlArea_PageToolBar_Add_";

		String CREATE_PAGE_TITLE = "input#TitleInput";

		String CREATE_PAGE_EDITOR_CONTAINER_ID = "UIEditorTabs";

		String CREATE_PAGE_EDITOR_AREA_CSS = "textarea#Markup";

		String CREATE_PAGE_HELP_AREA_ID = "UIWikiSidePanelArea";

		String TITLE_WATCH_DIALOG_FORM = "//span[@class='PopupTitle' and text()='Messages']";

		String WATCH_DIALOG_FORM = "//div[@class='UIPopupWindow UIDragObject ExoMessageDecorator']";

		String MOVE_PAGE_FORM_ID = "UIWikiPopupWindowL1";

		String IS_FIRST_ELEMENT_SELECT_IN_MOVE_WIN = "//div[@id='UIMoveTree']//div[@class='NodeGroup']/div[@class='LastNode Node']//div[@id='iconTreeExplorer' and @class='Wikihome TreeNodeType Node   Hover']/div/a[text()='Wiki Home']";

		String NODE_SELECTOR_MOVE_FORM = "//div[@id='UIMoveTree']//div[@class='NodeLabel']//a[@title='%s']";

		String FIRST_DOC_IN_THREE_IN_DESIRED_POSITION = "//div[@class='NodeLabel']/a[@title='%s']";

		String SECOND_DOC_IN_THREE_IN_DESIRED_POSITION = "//div[@class='LastNode Node']//div[@class='NodeLabel']/a[@title='%s']";

		String SEARCH_WIKI_ID = "wikiSearchValue";

		String SEARCH_RESULT_BY_PAGENAME = "//div[@class=\"UIWikiAdvanceSearchResult\"]//a[contains(.,\"%s\")]";

	}

	// web elements base elements on wiki home page
	@FindBy(className = Locators.LEFT_AREA_WIKIHOME)
	private WebElement leftAreaWikiHome;

	@FindBy(id = Locators.CENTRAL_AREA_WIKIHOME)
	private WebElement mainMenuWikiHome;

	// web elements add wiki
	@FindBy(id = Locators.ADD_PAGE_MENU_ID)
	private WebElement addPageMenu;

	@FindBy(css = Locators.CREATE_PAGE_TITLE)
	private WebElement titleAddPage;

	@FindBy(id = Locators.CREATE_PAGE_EDITOR_CONTAINER_ID)
	private WebElement editorBtnContainer;

	@FindBy(id = Locators.CREATE_PAGE_HELP_AREA_ID)
	private WebElement helpContainer;

	@FindBy(css = Locators.CREATE_PAGE_EDITOR_AREA_CSS)
	private WebElement textDocContainer;

	@FindBy(className = Locators.CONTENT_WIKIHOME_DOC_CLASS)
	private WebElement homePageWikiContent;

	@FindBy(id = Locators.TITLE_ID_WIKIHOME_DOC)
	private WebElement homePageWikiTitle;

	@FindBy(css = Locators.EDIT_BTN)
	private WebElement editBtn;

	@FindBy(id = Locators.MORE_MENU_ID)
	private WebElement moreMenu;

	@FindBy(xpath = Locators.WATCH_DIALOG_FORM)
	private WebElement watchDialogWin;

	@FindBy(xpath = Locators.TITLE_WATCH_DIALOG_FORM)
	private WebElement titleWatchDialogWin;

	@FindBy(id = Locators.MOVE_PAGE_FORM_ID)
	private WebElement moveFormID;

	@FindBy(xpath = Locators.IS_FIRST_ELEMENT_SELECT_IN_MOVE_WIN)
	private WebElement isFirstElemSelectMoveWin;

	@FindBy(id = Locators.SEARCH_WIKI_ID)
	private WebElement searchField;

	/**
	 * wait wiki page for add new document
	 * 
	 * @throws Exception
	 */
	public void waitAddWiki() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return helpContainer != null && helpContainer.isDisplayed()
						&& editorBtnContainer != null
						&& editorBtnContainer.isDisplayed()
						&& titleAddPage != null && titleAddPage.isDisplayed();
			}
		});
	}

	/**
	 * wait wiki page for add new document
	 * 
	 * @throws Exception
	 */
	public void watchDialogOpen() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return watchDialogWin != null && watchDialogWin.isDisplayed();
			}
		});
	}

	/**
	 * wait document in first position with the specified name
	 * 
	 * @param docname
	 * @throws Exception
	 */
	public void waitDocInFirstPosition(final String docname) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement doc = driver.findElement(By.xpath(String.format(
						Locators.FIRST_DOC_IN_THREE_IN_DESIRED_POSITION,
						docname)));
				return doc != null && doc.isDisplayed();
			}
		});
	}

	/**
	 * wait document in second position with the specified name
	 * 
	 * @param docname
	 * @throws Exception
	 */
	public void waitDocInSecondPosition(final String docname) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement doc = driver.findElement(By.xpath(String.format(
						Locators.SECOND_DOC_IN_THREE_IN_DESIRED_POSITION,
						docname)));
				return doc != null && doc.isDisplayed();
			}
		});
	}

	/**
	 * wait selecting root element in tree move form
	 * 
	 * @throws Exception
	 */
	public void waitSelectRootElemMoveForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return isFirstElemSelectMoveWin != null
						&& isFirstElemSelectMoveWin.isDisplayed();
			}
		});
	}

	/**
	 * wait move page form for add new document
	 * 
	 * @throws Exception
	 */
	public void waitMovePageFormOpen() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return moveFormID != null && moveFormID.isDisplayed();
			}
		});
	}

	/**
	 * wait move page form closed
	 * 
	 * @throws Exception
	 */
	public void waitMovePageFormClosed() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					driver.findElement(By.id(Locators.MOVE_PAGE_FORM_ID));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait wiki page for add new document
	 * 
	 * @throws Exception
	 */
	public void watchDialogclose() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return driver.findElement(
							By.xpath(Locators.TITLE_WATCH_DIALOG_FORM))
							.isDisplayed();
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait link with with the specified name
	 * 
	 * @param link
	 * @throws Exception
	 */
	public void waitLink(final String link) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.linkText(String.format(
						Locators.UNIQUE_LINK_NAME, link)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	public void waitWikiHome() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {

				return leftAreaWikiHome != null
						&& leftAreaWikiHome.isDisplayed()
						&& mainMenuWikiHome != null
						&& mainMenuWikiHome.isDisplayed() && moreMenu != null
						&& moreMenu.isDisplayed();
			}
		});
	}

	/**
	 * move mouse to add page menu
	 * 
	 * @throws Exception
	 */
	public void moveToAddPageMenu() throws Exception {
		new Actions(driver).moveToElement(addPageMenu).build().perform();
	}

	/**
	 * move mouse to add page menu
	 * 
	 * @throws Exception
	 */
	public void moveToMoreMenu() throws Exception {
		new Actions(driver).moveToElement(moreMenu).build().perform();
	}

	/**
	 * click on link with with specified name
	 * 
	 * @throws Exception
	 */
	public void clickOnLink(String nameLink) throws Exception {
		driver.findElement(
				By.linkText(String.format(Locators.UNIQUE_LINK_NAME, nameLink)))
				.click();
	}

	/**
	 * set name create wiki doc
	 * 
	 * @param name
	 */
	public void typeNameWikiDoc(String name) {
		titleAddPage.clear();
		titleAddPage.sendKeys(name);
	}

	/**
	 * type new contetent in wiki-doc
	 * 
	 * @param name
	 */
	public void typeTextDoc(String content) {
		textDocContainer.sendKeys(content);
	}

	/**
	 * get text from title document in wiki home page
	 * 
	 * @return
	 */
	public String getTitleDocOnHomeWiki() {
		return homePageWikiTitle.getText();
	}

	/**
	 * get text from content document in wiki home page
	 * 
	 * @return
	 */
	public String getContentDocHomeWiki() {
		return homePageWikiContent.getText();
	}

	/**
	 * get text from content document in wiki home page
	 * 
	 * @return
	 */
	public void cliclOnContentDocAddWiki() {
		textDocContainer.click();
	}

	/**
	 * get text from content document in wiki home page
	 * 
	 * @return
	 */
	public void clicklOnEditDocMenu() {
		editBtn.click();
	}

	/**
	 * click in item in move form three
	 * 
	 * @param item
	 */
	public void clicklOnMoveTreeItem(String item) {
		driver.findElement(
				By.xpath(String.format(Locators.NODE_SELECTOR_MOVE_FORM, item)))
				.click();
	}

	public void isElemFirstPositionThreePresent(String name) {
		driver.findElement(
				By.xpath(String.format(Locators.NODE_SELECTOR_MOVE_FORM, name)))
				.click();
	}

	public void searchWikiPage(String text) {
		searchField.sendKeys(text);
	}

	public void createWikiPage(String pageName, String content)
			throws Exception {
		CW.WIKI.moveToAddPageMenu();
		CW.WIKI.waitLink("Blank Page");
		CW.WIKI.clickOnLink("Blank Page");
		CW.WIKI.waitAddWiki();
		CW.WIKI.typeNameWikiDoc(pageName);
		CW.WIKI.typeTextDoc(content);
		CW.WIKI.clickOnLink("Save");
		CW.WIKI.waitWikiHome();
		CW.WIKI.waitLink(pageName);
		assertEquals(pageName, CW.WIKI.getTitleDocOnHomeWiki());
		assertEquals(content, CW.WIKI.getContentDocHomeWiki());
	}

	/**
	 * wait for search result with name
	 */
	public void waitSearchResult(final String pageName) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(
						By.xpath(String.format(
								Locators.SEARCH_RESULT_BY_PAGENAME, pageName)))
						.isDisplayed();
			}
		});
	}

}
