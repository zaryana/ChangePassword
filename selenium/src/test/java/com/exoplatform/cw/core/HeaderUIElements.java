package com.exoplatform.cw.core;

import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.exoplatform.cw.BaseTest;

public class HeaderUIElements extends BaseTest {

	private interface Locators {
		String UI_HEADER_ID = "UIHeader";

		String HEADER_LINKS_CLASS = "MainContent";

		String EXO_LOGO_LOCATOR = "//img[@src='/background/logo.png']";

		String ABOUT_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='About']";

		String FORUM_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Forum'] ";

		// for old versions
		// String HELP =
		// "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Help'] ";

		String HELP = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='FAQ'] ";

		String EXO_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='eXoplatform.com']";

		String CONTACT_AS_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Contact us']";

		String HEADER_CLOUD_WORKSPACES = "div.ClouIntranet>h1";

		String SPAN_CLOUD_WORKSPACES = "div.ClouIntranet>span";

		String FIRST_NAME = "first_name";

		String LAST_NAME = "last_name";

		String YOUR_MAIL = "email";

		String PHONE = "phone_work";

		String SUBJECT = "subject";

		String MESSAGE = "message";

		String SEND_BUTTON = "submitButton";
		
		String MAIN_ABOUT = "/html/body/div/div[4]/div";
		
		String MAIN_FORUM = "/html/body/div/div[2]/div/div/div/div/div/div/div/div/div/div/div/div/div/div/div[2]/div/div/div/div/div/div/div/div/div/div/div[3]/div";
		
		String MAIN_FAQ = "/html/body/div/div[4]/div/div/a/img";
		
		String MAIN_EXO = "company-navigation";
		
		String MAIN_CONTACT_US = "formDisplay";
		
		String MAIN_THANK = "/html/body/div/div[4]/div";
	

	}

	// WebElemnts Base Header Elements
	@FindBy(id = Locators.UI_HEADER_ID)
	private WebElement headerContainer;

	@FindBy(className = Locators.HEADER_LINKS_CLASS)
	private WebElement hederMainLinksContainer;

	@FindBy(xpath = Locators.EXO_LOGO_LOCATOR)
	private WebElement exoLogo;

	@FindBy(xpath = Locators.ABOUT_LINK)
	private WebElement about;

	@FindBy(xpath = Locators.FORUM_LINK)
	private WebElement forum;

	@FindBy(xpath = Locators.HELP)
	private WebElement help;

	@FindBy(xpath = Locators.EXO_LINK)
	private WebElement mainLink;

	@FindBy(xpath = Locators.CONTACT_AS_LINK)
	private WebElement contactAs;

	@FindBy(css = Locators.HEADER_CLOUD_WORKSPACES)
	private WebElement firstHeader;

	@FindBy(css = Locators.SPAN_CLOUD_WORKSPACES)
	private WebElement secondHeader;
	
	@FindBy(id = Locators.FIRST_NAME)
	private WebElement firstNameField;

	@FindBy(id = Locators.LAST_NAME)
	private WebElement lastNameField;

	@FindBy(id = Locators.PHONE)
	private WebElement phoneField;

	@FindBy(id = Locators.YOUR_MAIL)
	private WebElement emailField;

	@FindBy(id = Locators.MESSAGE)
	private WebElement messageField;

	@FindBy(id = Locators.SUBJECT)
	private WebElement subjectField;

	@FindBy(id = Locators.SEND_BUTTON)
	private WebElement sendbutton;

		
	/**
	 * wait base elements of sign form
	 * 
	 * @throws Exception
	 */
	public void waitHeaderElements() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				try {
					return headerContainer != null
							&& headerContainer.isDisplayed()
							&& hederMainLinksContainer != null
							&& hederMainLinksContainer.isDisplayed()
							&& exoLogo != null && exoLogo.isDisplayed()
							&& about != null && about.isDisplayed()
							&& forum != null && forum.isDisplayed()
							&& help != null && help.isDisplayed()
							&& mainLink != null && mainLink.isDisplayed()
							&& contactAs != null && contactAs.isDisplayed();
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public String getHeader() {
		String header = firstHeader.getText() + "\n" + secondHeader.getText();
		return header;
	}

	
	//About
	
	public void moveMouseToAbout() {
		new Actions(driver).moveToElement(about).build().perform();
	}
	
	public void cursorAbout(){
		String cursor = about.getCssValue("cursor");
		String curCur = "pointer";
		curCur.equals(cursor);
	}
	
	public void clickOnAbout() {	
		about.click();
	}

	public void waitAboutElement() {
		(new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				try {
					return d.findElement(By  
							.xpath(Locators.MAIN_ABOUT)) 
							.isDisplayed();
				} catch (NoSuchElementException e) {
					return false;
				}
			}
		});
	}

	
	//Forum
	
	public void moveMouseToForum() {
		new Actions(driver).moveToElement(forum).build().perform();
	}
	
	public void cursorForum(){
		String cursor = forum.getCssValue("cursor");
		String curCur = "pointer";
		curCur.equals(cursor);
	}
	
	public void clickOnForum() {
		forum.click();
	}
	
	
	public void switchWin() {
		for (String handle : driver.getWindowHandles()) {
		    driver.switchTo().window(handle);
		}
	}
	
	public void waitForumElement() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.xpath(Locators.MAIN_FORUM)).isDisplayed();
			}
		});
		
	}
	
	public void closeWin() {
		driver.close();
	}
	

	//Faq
	
	public void moveMouseToFaq() {
		new Actions(driver).moveToElement(help).build().perform();
	}
	
	public void cursorFaq(){
		String cursor = help.getCssValue("cursor");
		String curCur = "pointer";
		curCur.equals(cursor);
	}
	
	public void clickOnFaq() {
		help.click();
	}
	
	public void waitFaqElement() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.xpath(Locators.MAIN_FAQ)).isDisplayed();
			}
		});
		
	}
	
	
	//Exo
	
	public void moveMouseToExo() {
		new Actions(driver).moveToElement(mainLink).build().perform();
	}
	
	public void cursorExo(){
		String cursor = mainLink.getCssValue("cursor");
		String curCur = "pointer";
		curCur.equals(cursor);
	}
	
	public void clickOnExo() {
		mainLink.click();
	}
	
	public void waitExoElement() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.id(Locators.MAIN_EXO)).isDisplayed();
			}
		});
		
	}
	
	
	//Contact Us
	
	public void moveMouseToContactUs() {
		new Actions(driver).moveToElement(contactAs).build().perform();
	}
	
	public void cursorContactUs(){
		String cursor = contactAs.getCssValue("cursor");
		String curCur = "pointer";
		curCur.equals(cursor);
	}
	
	public void clickOnContactUs() {
		contactAs.click();
	}
	
	public void waitContactUsElement() throws Exception {
		new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver elem) {
				return driver.findElement(By.id(Locators.MAIN_CONTACT_US)).isDisplayed();
			}
		});
		
	}
	
	 public void typeUserFirstName() { 
		 firstNameField.sendKeys("ghd"); }
	 
	 public void typeUserLastName() { 
		 lastNameField.sendKeys("hgfhd"); }
	 
	 public void typeUserEmail() { 
		 emailField.sendKeys("zaryana.dombrovkayag@gmail.com"); }
	 
	 public void typeUserPhone() { 
		 phoneField.sendKeys("45645"); }
	 
	 public void typeUserSubject() { 
		 subjectField.sendKeys("kguukdty"); }
	 
	 public void typeUserMessage() { 
		 messageField.sendKeys("ufiju ghk hjfkg ghbdfz полдпр"); }
	 
	 public void typeSubmitButton() { 
		 sendbutton.click(); }
	 
		public void waitThankElement() throws Exception {
			new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver elem) {
					return driver.findElement(By.xpath(Locators.MAIN_THANK)).isDisplayed();
				}
			});
			
		}

}
