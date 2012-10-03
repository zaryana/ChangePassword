package com.exoplatform.cloudworkspaces.webui;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.exoplatform.cw.BaseTest;
import com.exoplatform.cw.CW;


public class CheckMenu extends BaseTest {
	
	@BeforeClass
	public static void start() throws Exception {
		// Choose browser Web driver:
		switch (BROWSER_COMMAND) {
		case GOOGLE_CHROME:

			System.setProperty("webdriver.chrome.driver",
					"src/test/resources/chromedriver");
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability("chrome.switches",
					Arrays.asList("--start-maximized"));
			driver = new ChromeDriver(capabilities);

			break;
		case IE_EXPLORE_PROXY:
			driver = new InternetExplorerDriver();
			break;
		default:
			driver = new FirefoxDriver();
		}

		CW = new CW(selenium(), APPLICATION_URL, driver);

		try {
			if (CW_SETTINGS.getString("selenium.browser.commad").equals(
					"CHROME")) {
				driver.manage().window().maximize();
			}
			driver.get(APPLICATION_URL);

		} catch (Exception e) {
		}
	}
	
	@AfterClass
	public static void killFireFox() throws Exception {
		driver.close();
	}
	
	
	@Test
	public void MenuTest() throws Exception {
		CW.HEADER.waitHeaderElements();
		CW.HEADER.moveMouseToAbout();
		CW.HEADER.clickOnAbout();
		CW.HEADER.waitAboutElement();
		CW.HEADER.moveMouseToForum();
		CW.HEADER.cursorForum();
		CW.HEADER.clickOnForum();
		CW.HEADER.switchWin();
		CW.HEADER.waitForumElement();
		CW.HEADER.closeWin();
		CW.HEADER.switchWin();
		CW.HEADER.moveMouseToFaq();
		CW.HEADER.cursorFaq();
		CW.HEADER.clickOnFaq();
		CW.HEADER.switchWin();
		CW.HEADER.waitFaqElement();
		CW.HEADER.closeWin();
		CW.HEADER.switchWin();
		CW.HEADER.moveMouseToExo();
		CW.HEADER.cursorExo();
		CW.HEADER.clickOnExo();
		CW.HEADER.switchWin();
		CW.HEADER.waitExoElement();
		CW.HEADER.closeWin();
		CW.HEADER.switchWin();
		CW.HEADER.moveMouseToContactUs();
		CW.HEADER.cursorContactUs();
		CW.HEADER.clickOnContactUs();
		CW.HEADER.waitContactUsElement();
		CW.HEADER.typeUserFirstName();
		CW.HEADER.typeUserLastName();
		CW.HEADER.typeUserEmail();
		CW.HEADER.typeUserPhone();
		CW.HEADER.typeUserSubject();
		CW.HEADER.typeUserMessage();
		CW.HEADER.typeSubmitButton();
		CW.HEADER.switchWin();
		CW.HEADER.waitThankElement();
		CW.HEADER.closeWin();
	}
	
	
}
