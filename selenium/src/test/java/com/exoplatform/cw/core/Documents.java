package com.exoplatform.cw.core;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

public class Documents extends BaseTest {

	// Locators basic elements Dialog About Menu
	private interface Locators {
		String LAST_ELEMENT_ON_PAGE = "div.UIListGrid";

		String UPLOAD_DOCUMENT_LINK = "a[title=Upload]";

		String LAST_DIV_ON_UPLOAD_FORM = "//span[@class='PopupTitle' and text()='Upload File']";

		String UPLOAD_FILE_FIELD_ID = "input#file";

		String UPLOAD_IFRAME = "div.UploadIframe>iframe";

		String UPLOAD_MANADGER_FORM = "UIUploadManager";

		String UNIQUE_LINK_NAME = "%s";

		String UPLOAD_FILE_NAME_LABEL = "//div[@class='FileNameLabel' and text()=' %s']";

		String NODE_TREE_FILE_VISIBLE = "div.NodeLabel>a[title='%s ']";

		String DMS_MENU_METADATA = "div.Split>a[title=Metadata]";

		String DMS_MENU_DELETE = "div.Split>a[title=Delete]";

		String DMS_MENU_EDIT = "div.Split>a[title=Edit]";

		String DMS_MENU_TAG = "div.Split>a[title=Tag]";

		String DMS_MENU_VOTE = "div.Split>a[title=Vote]";

		String DMS_MENU_WATCH_UNWATCH = "div.Split>a[title='Watch/Unwatch']";

		String CONFIRM_DELETE_DIALOG_TITLE = "//span[text()='Confirm Deletion']";

		String CONFIRM_DELETE_DIALOG_OK_BTN = "OK";

		String CONFIRM_DELETE_DIALOG_CLOSE_BTN = "Close";

		String CONFIRM_DELETE_DIALOG_CLOSE_WINDOW = "a.CloseButton";

		String UI_POPUP_WINDOW = "UIPopupWindow";

		String TITLE_NEW_FOLDERFIELD_ID = "input#title";

		String NAME_FOLDER_FIELD = "input#name";

		String NEW_FOLDER_FORM_ID = "UIFolderForm";

		// -------------------------------------------
		String SHOW_DRIVERS_BUTTON = "a[title='Show Drives']";

		String DRIVERS_CONTAINER = "UIDrivesArea";

		String DOCUMENT_CONTENT = "//div[@id=\"UIDocumentContainer\"]//div[@class=\"Content\"]/pre";

		String DELETE_FOLDER = "//div[@class=\"Non-Split\"]//a[contains(.,\"Delete\")]";

		String POP_UP_BUTTONS_BY_NAME = "//div[@class=\"UIAction\"]/a[contains(.,\"%s\")]";

		String POP_UP_CONTENT = "//span[@class=\"PopupIcon InfoMessageIcon\" and contains(.,\"%s\")]";

		String VOTE_DOCUMENT = "//div[@class=\"UIRatingInfoContainer\"]//div[contains(@title, \"%s\")]";

		String VOTED_INFO = "//div[@class=\"VotedInfo\"]";
	}

	// basic page elements
	@FindBy(css = Locators.LAST_ELEMENT_ON_PAGE)
	private WebElement controlElem;

	@FindBy(css = Locators.UPLOAD_DOCUMENT_LINK)
	private WebElement uploadDoc;

	@FindBy(css = Locators.UPLOAD_FILE_FIELD_ID)
	private WebElement uploadField;

	@FindBy(xpath = Locators.LAST_DIV_ON_UPLOAD_FORM)
	private WebElement uploadForm;

	@FindBy(css = Locators.UPLOAD_IFRAME)
	private WebElement uploadIframe;

	@FindBy(xpath = Locators.UPLOAD_FILE_NAME_LABEL)
	private WebElement nameUpldFile;

	@FindBy(css = Locators.DMS_MENU_METADATA)
	private WebElement dmsMenuMetadata;

	@FindBy(css = Locators.DMS_MENU_DELETE)
	private WebElement dmsMenuDelete;

	@FindBy(css = Locators.DMS_MENU_EDIT)
	private WebElement dmsMenuEdit;

	@FindBy(css = Locators.DMS_MENU_TAG)
	private WebElement dmsMenuTag;

	@FindBy(css = Locators.DMS_MENU_WATCH_UNWATCH)
	private WebElement dmsWatchUnwatch;

	@FindBy(xpath = Locators.CONFIRM_DELETE_DIALOG_TITLE)
	private WebElement confirmDeleteTitle;

	@FindBy(linkText = Locators.CONFIRM_DELETE_DIALOG_OK_BTN)
	private WebElement confirmDeleteOkBtn;

	@FindBy(linkText = Locators.CONFIRM_DELETE_DIALOG_CLOSE_BTN)
	private WebElement confirmDeleteCloseBtn;

	@FindBy(css = Locators.CONFIRM_DELETE_DIALOG_CLOSE_WINDOW)
	private WebElement confirmCloseWindow;

	@FindBy(id = Locators.UI_POPUP_WINDOW)
	private WebElement uiPopupWindow;

	@FindBy(id = Locators.UPLOAD_MANADGER_FORM)
	private WebElement uploadManadgerForm;

	@FindBy(id = Locators.NEW_FOLDER_FORM_ID)
	private WebElement newFolderForm;

	@FindBy(css = Locators.NAME_FOLDER_FIELD)
	private WebElement newFolderFormName;

	@FindBy(css = Locators.TITLE_NEW_FOLDERFIELD_ID)
	private WebElement newFolderFormTitle;

	@FindBy(css = Locators.SHOW_DRIVERS_BUTTON)
	private WebElement drivesBtn;

	@FindBy(id = Locators.DRIVERS_CONTAINER)
	private WebElement drivesContainer;

	@FindBy(xpath = Locators.DOCUMENT_CONTENT)
	private WebElement contentDocContainer;

	@FindBy(xpath = Locators.DELETE_FOLDER)
	private WebElement deleteFolderButton;

	/**
	 * wait opening of document page
	 * 
	 * @throws Exception
	 */
	public void waitDocumentsOpen() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return controlElem != null && controlElem.isDisplayed();
			}
		});
	}

	/**
	 * wait opened of content file
	 * 
	 * @throws Exception
	 */
	public void waitDocContainer() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {

				return contentDocContainer != null
						&& contentDocContainer.isDisplayed();
			}
		});
	}

	/**
	 * wait opening upload manager form
	 * 
	 * @throws Exception
	 */
	public void waitUploadManagerForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {

				return uploadManadgerForm != null
						&& uploadManadgerForm.isDisplayed();
			}
		});
	}

	/**
	 * wait link contain text
	 * 
	 * @param link
	 * @throws Exception
	 */
	public void waitNewLinkAppearWithPrefix(final String link) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement lnk = driver.findElement(By.partialLinkText(String
						.format(Locators.UNIQUE_LINK_NAME, link)));
				return lnk != null && lnk.isDisplayed();
			}
		});
	}

	/**
	 * wait opening drives page
	 * 
	 * @throws Exception
	 */
	public void waitDrivesPage() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return drivesContainer != null && drivesContainer.isDisplayed();
			}
		});
	}

	/**
	 * wait opening upload manager form
	 * 
	 * @throws Exception
	 */
	public void waitNewFolderForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return newFolderForm != null && newFolderForm.isDisplayed()
						&& newFolderFormName != null
						&& newFolderFormName.isDisplayed()
						&& newFolderFormTitle != null
						&& newFolderFormTitle.isDisplayed();
			}
		});
	}

	/**
	 * wait Create New Folder form manager form
	 * 
	 * @throws Exception
	 */
	public void waitCloseNewFolderForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement view = driver.findElement(By
							.id(Locators.NEW_FOLDER_FORM_ID));
					return false;
				} catch (Exception e) {
					return true;
				}

			}
		});
	}

	/**
	 * wait closed upload Manadger wait opening of document page
	 * 
	 * @throws Exception
	 */
	public void waitClosedUploadManagerForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {

				try {
					WebElement view = driver.findElement(By
							.id(Locators.UPLOAD_MANADGER_FORM));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait appearance link with unique name on page
	 * 
	 * @throws Exception
	 */
	public void waitLinkAppear(final String nameLink) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement view = driver.findElement(By.linkText(String
							.format(Locators.UNIQUE_LINK_NAME, nameLink)));
					return true;
				} catch (Exception e) {
					return false;
				}

			}
		});
	}

	/**
	 * wait opening of document page
	 * 
	 * @throws Exception
	 */
	public void waitDmsMenu() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return dmsMenuMetadata != null && dmsMenuMetadata.isDisplayed()
						&& dmsMenuEdit != null && dmsMenuEdit.isDisplayed()
						&& dmsMenuDelete != null && dmsMenuDelete.isDisplayed()
						&& dmsMenuTag != null && dmsMenuTag.isDisplayed()
						&& dmsWatchUnwatch != null
						&& dmsWatchUnwatch.isDisplayed();
			}
		});
	}

	/**
	 * @throws Exception
	 */
	public void waitConfirmDeleteForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return confirmCloseWindow != null
						&& confirmCloseWindow.isDisplayed()
						&& confirmDeleteTitle != null
						&& confirmDeleteTitle.isDisplayed()
						&& confirmDeleteOkBtn != null
						&& confirmDeleteOkBtn.isDisplayed()
						&& confirmDeleteOkBtn != null
						&& confirmDeleteOkBtn.isDisplayed();
			}
		});
	}

	/**
	 * wait closing confirm delete dialog window
	 * 
	 * @throws Exception
	 */
	public void waitConfirmDeleteFormClosed() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement view = driver.findElement(By
							.id(Locators.UI_POPUP_WINDOW));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait opening of document page
	 * 
	 * @throws Exception
	 */
	public void waitUploadFile(final String name) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement upldfile = driver.findElement(By.xpath(String
						.format(Locators.UPLOAD_FILE_NAME_LABEL, name)));
				return upldfile != null && upldfile.isDisplayed();
			}
		});
	}

	/**
	 * wait appearance content in Node Three group
	 * 
	 * @throws Exception
	 */
	public void waitFileInNodeTree(final String name) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				WebElement visible = driver.findElement(By.cssSelector(String
						.format(Locators.NODE_TREE_FILE_VISIBLE, name)));
				return visible != null && visible.isDisplayed();
			}
		});
	}

	/**
	 * wait while file is deleted in node three
	 * 
	 * @throws Exception
	 */
	public void waitDeletingFileInNodeTree(final String name) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement visible = driver.findElement(By
							.cssSelector(String.format(
									Locators.NODE_TREE_FILE_VISIBLE, name)));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * wait upload form
	 * 
	 * @throws Exception
	 */
	public void waitOpenUploadForm() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return uploadForm != null && uploadForm.isDisplayed();
			}
		});
	}

	/**
	 * wait close upload form
	 * 
	 * @throws Exception
	 */
	public void waitClosedUploadForm(final String fileLabel) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					WebElement view = driver.findElement(By.xpath(String
							.format(Locators.UPLOAD_FILE_NAME_LABEL, fileLabel)));
					return false;
				} catch (Exception e) {
					return true;
				}
			}
		});
	}

	/**
	 * switch to Upload iframe form from file system
	 */
	public void selectIframeUpload() {
		driver.switchTo().frame(uploadIframe);
	}

	/**
	 * click for upload content from file system
	 */
	public void uploadClick() {
		uploadDoc.click();
	}

	/**
	 * set path for upload file from file system
	 */
	public void typePathToUploadField(String path) {
		uploadField.sendKeys(path);
	}

	/**
	 * click on link with unique name
	 * 
	 * @param name
	 */
	public void clickOnLink(String name) {
		driver.findElement(
				By.linkText(String.format(Locators.UNIQUE_LINK_NAME, name)))
				.click();

	}

	/**
	 * click on upload file in node three
	 * 
	 * @param name
	 */
	public void clickFileInNodeThree(String name) {
		driver.findElement(
				By.cssSelector(String.format(Locators.NODE_TREE_FILE_VISIBLE,
						name))).click();
	}

	/**
	 * type into title filed create new folder form
	 * 
	 * @param name
	 */
	public void typeTitleNewFolderField(String name) {
		newFolderFormTitle.sendKeys(name);
	}

	/**
	 * type into name filed create new folder form
	 * 
	 * @param name
	 */
	public void typeNameNewFolderField(String name) {
		newFolderFormName.sendKeys(name);
	}

	/**
	 * click on DrivesBtn
	 */
	public void drivesBtnClick() {
		drivesBtn.click();
	}

	/**
	 * @return tex from opened file
	 */
	public String getTetxFromDoc() {
		return contentDocContainer.getText();

	}

	/**
	 * click on delete folder button
	 */
	public void deleteFolder() {
		deleteFolderButton.click();
	}

	/**
	 * wait upload form
	 * 
	 * @throws Exception
	 */
	public void waitDeleteFolderButton() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return deleteFolderButton != null
						&& deleteFolderButton.isDisplayed();
			}
		});
	}

	/**
	 * click on WatchUnwatch button
	 */
	public void clickOnWatchUnwatchButton() {
		dmsWatchUnwatch.click();
	}

	/**
	 * click on popup buttons
	 * 
	 * @param name
	 */
	public void clickOnPopUpButton(String buttonName) {
		driver.findElement(
				By.xpath(String.format(Locators.POP_UP_BUTTONS_BY_NAME,
						buttonName))).click();
	}

	/**
	 * wait for pop up content
	 * 
	 * @throws Exception
	 */
	public void waitPopUpContent(final String text) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(
						By.xpath(String.format(Locators.POP_UP_CONTENT, text)))
						.isDisplayed();
			}
		});
	}

	/**
	 * wait for pop up button appear
	 */
	public void waitPopUpButton(final String buttonName) throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(
						By.xpath(String.format(Locators.POP_UP_BUTTONS_BY_NAME,
								buttonName))).isDisplayed();
			}
		});
	}

	/**
	 * wait for pop up button appear
	 */
	public void waitUIPopupWindow() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.id(Locators.UI_POPUP_WINDOW))
						.isDisplayed();
			}
		});
	}

	/**
	 * click on vote document stars values: Best, Excellent, Very Good, Good,
	 * Normal
	 * 
	 * @param name
	 */
	public void clickOnVoteStars(String starName) {
		driver.findElement(
				By.xpath(String.format(Locators.VOTE_DOCUMENT, starName)))
				.click();
	}

	/**
	 * wait for voted info appear
	 */
	public void waitVotedInfo() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.xpath(Locators.VOTED_INFO))
						.isDisplayed();
			}
		});
	}

	/**
	 * Upload document in documents
	 */
	public void uploadDocument(String path, String documentName)
			throws Exception {
		CW.DOCUMENTS.uploadClick();
		CW.DOCUMENTS.waitOpenUploadForm();
		CW.DOCUMENTS.selectIframeUpload();
		CW.DOCUMENTS.typePathToUploadField(path);
		selectMainFrame();
		CW.DOCUMENTS.waitUploadFile(documentName);
		CW.DOCUMENTS.clickOnLink("Save");
		CW.DOCUMENTS.waitClosedUploadForm(documentName);
		CW.DOCUMENTS.waitUploadManagerForm();
		CW.DOCUMENTS.clickOnLink("Close");
		CW.DOCUMENTS.waitClosedUploadManagerForm();
		CW.DOCUMENTS.waitFileInNodeTree(documentName);
	}

}
