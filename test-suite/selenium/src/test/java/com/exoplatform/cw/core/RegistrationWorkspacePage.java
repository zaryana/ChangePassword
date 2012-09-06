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

import com.exoplatform.cw.BaseTest;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $
 */

public class RegistrationWorkspacePage extends BaseTest
{

   //Locators basic elements For Top Menu
   private interface Locators
   {
      String REGISTRATION_FORM_ID = "registrationForm";

      String EMAIL_INPUT = "//input[@id='email' and @value='yourname@yourcompany.com']";

      String FIRST_NAME = "first_name";

      String LAST_NAME = "last_name";

      String PHONE = "phone_work";

      String COMPANY = "company";

      String USERNAME = "username";

      String PASSWORD = "password";

      String CONFIRM_PASSWORD = "password2";

      String SUBMIT_BTN = "t_submit";

   }

   @FindBy(id = Locators.REGISTRATION_FORM_ID)
   private WebElement registrationFormBlock;

   @FindBy(xpath = Locators.EMAIL_INPUT)
   private WebElement emailField;

   @FindBy(id = Locators.FIRST_NAME)
   private WebElement firstNameField;

   @FindBy(id = Locators.LAST_NAME)
   private WebElement lastNameField;

   @FindBy(id = Locators.PHONE)
   private WebElement phoneField;

   @FindBy(id = Locators.COMPANY)
   private WebElement companyField;

   @FindBy(id = Locators.USERNAME)
   private WebElement usernameField;

   @FindBy(id = Locators.PASSWORD)
   private WebElement passwordField;

   @FindBy(id = Locators.CONFIRM_PASSWORD)
   private WebElement confirmPasswordField;

   @FindBy(id = Locators.SUBMIT_BTN)
   private WebElement submitBtn;

   /**
    * wait body container
    * @throws Exception
    */
   public void waitRegistrationForm() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {

            return registrationFormBlock != null && registrationFormBlock.isDisplayed() && emailField != null
               && emailField.isDisplayed() && firstNameField != null && firstNameField.isDisplayed()
               && lastNameField != null && lastNameField.isDisplayed() && phoneField != null
               && phoneField.isDisplayed() && companyField != null && companyField.isDisplayed()
               && usernameField != null && usernameField.isDisplayed() && passwordField != null
               && passwordField.isDisplayed() && confirmPasswordField != null && confirmPasswordField.isDisplayed()
               && submitBtn != null && submitBtn.isDisplayed();

         }
      });
   }

   /**
    * type to fires name field
    * @param name
    */
   public void typeToFirstNameField(String name)
   {

      firstNameField.sendKeys(name);
   }

   /**
    * type to last name field
   * @param lastName
   */
   public void typeToLastNameField(String lastName)
   {

      lastNameField.sendKeys(lastName);
   }

   /**
    * type to phone field
   * @param lastName
   */
   public void typeToPhoneField(String phone)
   {

      phoneField.sendKeys(phone);
   }

   /**
    * type to company field
   * @param lastName
   */
   public void typeToCompanyField(String company)
   {

      companyField.sendKeys(company);
   }

   /**
    * type to password field
   * @param lastName
   */
   public void typeToPasswordField(String pass)
   {

      passwordField.sendKeys(pass);
   }

   /**
    * type to confirm pass field
   * @param lastName
   */
   public void typeToConfirmPassField(String confirm_pass)
   {

      confirmPasswordField.sendKeys(confirm_pass);
   }

   /**
    * click on submit btn
   * @param lastName
   */
   public void clickOnSubmitBtn()
   {

      submitBtn.click();
   }

}
