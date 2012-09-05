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

import java.sql.Driver;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:mmusienko@exoplatform.com">Musienko Maksim</a>
 * @version $
 */

public class MyProfilePage extends BaseTest
{

   //Locators basic elements Dialog About Menu
   private interface Locators
   {

      String CHANGE_AVATAR_LINK = "Change Picture";

      String INPUT_FIELD_ID = "file";

      String BUTTONS_CONTAINER_CLASS = "UIAction";

      String UPLOAD_INPUT_CLASS = "UIUploadForm";

      String CONFIRM_LINK = "Confirm";

      String UPLOAD_IFRAME_CLASS = "div.UploadIframe>iframe";

      String SAVE_AVATAR_BUTTON_LINK = "Save";

      String AVATAR_IMAGE_CLASS = "ImageAvatar";

   }

   //basic page elements
   @FindBy(linkText = Locators.CHANGE_AVATAR_LINK)
   private WebElement changeAvatar;

   @FindBy(className = Locators.UPLOAD_INPUT_CLASS)
   private WebElement uploadInput;

   @FindBy(name = Locators.INPUT_FIELD_ID)
   private WebElement uploadField;

   @FindBy(linkText = Locators.CONFIRM_LINK)
   private WebElement uploadBtn;

   @FindBy(css = Locators.UPLOAD_IFRAME_CLASS)
   private WebElement uploaIframe;

   @FindBy(className = Locators.BUTTONS_CONTAINER_CLASS)
   private WebElement buttons;

   @FindBy(className = Locators.AVATAR_IMAGE_CLASS)
   private WebElement avatarImage;

   @FindBy(linkText = Locators.SAVE_AVATAR_BUTTON_LINK)
   private WebElement saveAvatarButton;

   /**
    * @param numMess
    * @throws Exception
    *  wait link for avatar
    *  from CW page
    *  
    */
   public void waitConfirmSaveAvatarForm() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {

               return saveAvatarButton != null && saveAvatarButton.isDisplayed() && avatarImage != null
                  && avatarImage.isDisplayed();

            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   /**
    * @param numMess
    * @throws Exception
    *  wait link for avatar
    *  from CW page
    *  
    */
   public void waitChangeAvatarLink() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return changeAvatar != null && changeAvatar.isDisplayed();
         }
      });
   }

   /**
    * @param numMess
    * @throws Exception
    *  wait bottom container 
    *  load-file form
    *
    * */
   public void waitBottomLoadForm() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {

               return buttons != null && buttons.isDisplayed();

            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   /**
    * @param numMess
    * @throws Exception
    *  wait basic elements 
    *  for load files form
    *  
    */
   public void waitFormLoadFile() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {

               return uploadField != null && uploadField.isDisplayed() && uploadInput != null
                  && uploadInput.isDisplayed();

            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   /**
    * 
    * @throws Exception
    */
   public void switchToUploadIframe() throws Exception
   {
      driver.switchTo().frame(uploaIframe);
   }

   /**
    * click on upload btn link
    * @throws Exception
    */
   public void confirmClick() throws Exception
   {
      uploadBtn.click();
   }

   /**
    * click on avatar link
    * @throws Exception
    */
   public void clickChangeAvatar() throws Exception
   {
      changeAvatar.click();
   }

   /**
    * type path to file input field
    * @throws Exception
    */
   public void typeToLoadFileInput(String path) throws Exception
   {
      uploadField.sendKeys(path);
   }

   /**
    * click on save avatar button
    * @throws Exception
    */
   public void clickSaveAvatar() throws Exception
   {
      saveAvatarButton.click();
   }

}
