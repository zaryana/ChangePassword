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

public class CwLoginPage extends BaseTest
{

   //Locators basic elements Dialog About Menu
   private interface Locators
   {

      String LOGIN_FORM = "UILogin";

      String USER_NAME_FIELD = "username";

      String PASSWORD_FIELD = "password";

      String REMEMBE_ME_CHEKBOX = "rememberme";

      String CONTAINER_LOGINFORM_STAGING_CLASS = "UIPageBodyContainer";

      String LOGIN_BTN_STAGING = "UIPortalLoginFormAction";

      //String SIGN_IN_LINK="Sign in";

   }

   //basic page elements
   @FindBy(className = Locators.LOGIN_FORM)
   private WebElement loginForm;

   @FindBy(id = Locators.USER_NAME_FIELD)
   private WebElement userName;

   @FindBy(id = Locators.PASSWORD_FIELD)
   private WebElement password;

   @FindBy(id = Locators.REMEMBE_ME_CHEKBOX)
   private WebElement rememberChekbox;

   @FindBy(id = Locators.LOGIN_BTN_STAGING)
   private WebElement signInBtn;

   @FindBy(className = Locators.CONTAINER_LOGINFORM_STAGING_CLASS)
   private WebElement loginContainer;

   @FindBy(id = Locators.LOGIN_BTN_STAGING)
   private WebElement loginBtnStaging;

   /**
    * wait base elements on staging
    * Login form
    * @throws Exception
    */
   public void waitAppearLoginPageStaging() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {

            return userName.isDisplayed() && userName != null
               && password != null && password.isDisplayed() && loginContainer != null && loginContainer.isDisplayed()
               && loginBtnStaging != null && loginBtnStaging.isDisplayed();

         }
      });
   }

   /**
   * wait base elements of sign form
   * @throws Exception
   */
   public void waitAppearLoginPage() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {
               return loginForm != null && loginForm.isDisplayed() && userName.isDisplayed() && userName != null
                  && password != null && password.isDisplayed() && rememberChekbox != null
                  && rememberChekbox.isDisplayed() && signInBtn != null && signInBtn.isDisplayed();
            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   /**
    * type user name from conf file
    */
   public void typeUserName()
   {
      userName.sendKeys(USER_NAME);
   }

   /**
    * type second user name from conf file
    */
   public void typeSecondUserName()
   {
      userName.sendKeys(SECOND_USER_NAME);
   }

   /**
    * type user password from conf file 
    */
   public void typeUserPass()
   {
      password.sendKeys(USER_PASSWORD);
   }

   /**
    * type user password from conf file 
    */
   public void typeSecondUserPass()
   {
      password.sendKeys(SECOND_USER_PASSWORD);
   }

   /**
    *  click in sign in button 
    */
   public void clickOnSignBtn()
   {
      signInBtn.click();
   }

   /**
    *  click on login btn in staging
    */
   public void clickOnLoginBtnStg()
   {
      loginBtnStaging.click();
   }

}