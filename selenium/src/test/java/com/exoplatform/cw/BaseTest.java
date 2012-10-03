/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package com.exoplatform.cw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.thoughtworks.selenium.Selenium;

/**
 * Created by The eXo Platform SAS.
 * 
 * 
 * 
 * 
 * @author <a href="mailto:dmitry.@exoplatform.com"></a>
 * @version $Id: ${date} ${time}
 * 
 */
@RunWith(RCRunner.class)
public abstract class BaseTest {
	public static final ResourceBundle CW_SETTINGS = ResourceBundle
			.getBundle("conf/cw-selenium");

	public static String CW_HOST = CW_SETTINGS.getString("cw.host");

	public static String CW_TENANT = CW_SETTINGS.getString("cw.tenant");

	public static final int CW_PORT = Integer.valueOf(CW_SETTINGS
			.getString("cw.port"));

	public static String BASE_URL = "http://" + CW_TENANT
			+ ((CW_PORT == 80) ? ("") : (":" + CW_PORT)) + "/";

	public static final String USER_NAME = CW_SETTINGS
			.getString("cw.user.root.name");

	public static final String USER_PASSWORD = CW_SETTINGS
			.getString("cw.user.root.password");

	public static final String SECOND_USER_NAME = CW_SETTINGS
			.getString("cw.seconduser.root.name");

	public static final String SECOND_USER_PASSWORD = CW_SETTINGS
			.getString("cw.seconduser.root.password");

	// Change passwod prop
	public static final String USER_PASSWORD_FOR_CHANGE = CW_SETTINGS
			.getString("cw.user.root.passwordForChange");

	protected static String APPLICATION_URL = BASE_URL;

	protected static String LOGIN_URL = BASE_URL
			+ CW_SETTINGS.getString("cw.login.url");

	// mail box names
	public static final String OWNER_MAIL = CW_SETTINGS
			.getString("cw.mail.name.owner");

	public static final String JOINED_MAIL = CW_SETTINGS
			.getString("cw.mail.name.joined");

	public static final String SELENIUM_PORT = CW_SETTINGS
			.getString("selenium.port");
	public static final String MAIL_HOST = CW_SETTINGS
			.getString("cw.corp.mail");

	// this two variables add after change in URL cw
	public static Selenium selenium;

	protected static final EnumBrowserCommand BROWSER_COMMAND = EnumBrowserCommand
			.valueOf(CW_SETTINGS.getString("selenium.browser.commad"));

	public static Selenium selenium() {
		return selenium;
	}

	protected static WebDriver driver;

	public static CW CW;

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

		loginAsFirstUser();

	}

	@AfterClass
	public static void killFireFox() throws Exception {
		logout();
		driver.close();
	}

	/**
	 * Calls selenium refresh method and waits for {@link TestConstants}
	 * .IDE_LOAD_PERIOD seconds.
	 * 
	 * After waits for {@link TestConstants}.SLEEP seconds (while all elements
	 * are drawing).
	 * 
	 * @throws Exception
	 */
	public void refresh() throws Exception {
		driver.navigate().refresh();
	}

	@AfterFailure
	public void captureScreenShotOnFailure(Throwable failure) {
		// Get test method name
		String testMethodName = null;
		for (StackTraceElement stackTrace : failure.getStackTrace()) {
			if (stackTrace.getClassName().equals(this.getClass().getName())) {
				testMethodName = stackTrace.getMethodName();
				break;
			}
		}

		try {
			byte[] sc = ((TakesScreenshot) driver)
					.getScreenshotAs(OutputType.BYTES);
			File parent = new File("target/screenshots");
			parent.mkdirs();
			File file = new File(parent, this.getClass().getName() + "."
					+ testMethodName + ".png");
			file.createNewFile();
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(sc);
			outputStream.close();
		} catch (WebDriverException e) {
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public void selectMainFrame() {
		driver.switchTo().defaultContent();
	}

	/**
	 * Login as first user
	 * 
	 * @throws Exception
	 */
	public static void loginAsFirstUser() throws Exception {
		CW.LOGIN_PAGE.waitAppearLoginPageStaging();
		CW.LOGIN_PAGE.typeUserName();
		CW.LOGIN_PAGE.typeUserPass();
		CW.LOGIN_PAGE.clickOnLoginBtnStg();
		CW.WORKSPACE.waitBasicElements();

	}

	/**
	 * Login as second user
	 * 
	 * @throws Exception
	 */
	public static void loginAsSecondUser() throws Exception {
		CW.LOGIN_PAGE.waitAppearLoginPageStaging();
		CW.LOGIN_PAGE.typeSecondUserName();
		CW.LOGIN_PAGE.typeSecondUserPass();
		CW.LOGIN_PAGE.clickOnLoginBtnStg();
		CW.WORKSPACE.waitBasicElements();
	}

	/**
	 * Logout
	 * 
	 * @throws Exception
	 */
	public static void logout() throws Exception {
		CW.TOPMENUS.waitUserMenu();
		CW.TOPMENUS.mooveMouseToUserForm();
		CW.TOPMENUS.waitUserMenuLogoutButton();
		CW.TOPMENUS.logoutUser();
	}
}