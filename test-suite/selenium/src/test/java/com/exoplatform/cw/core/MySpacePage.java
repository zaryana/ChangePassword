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

public class MySpacePage extends BaseTest
{

   //Locators basic elements For Top Menu
   private interface Locators
   {
      String CONTAINER_MESSAGE_APPEAR = "div.CommentBlockBoundNone";

      String ADD_NEW_SPACE_LINK = "Add New Space";

      String NAME_SPACE_PREFIX = "//div[@class='TitleContent']/a[text()='%s']";

      String CREATE_SPACE_BUTTON = "Create";

      String NAME_SPACE_FIELD_ID = "displayName";

      String FIRST_SPACE = "//div[@class='BoxSpaceList ClearFix']";

      String SECOND_SPACE = "//div[@class='GrayBox ClearFix FL']";

      String NAME_OF_FIRST_SPACE = FIRST_SPACE + NAME_SPACE_PREFIX;

      String NAME_OF_SECOND_SPACE = NAME_SPACE_PREFIX;

      String LINKS_OF_FIRST_SPASE = FIRST_SPACE + "//ul[@class=\"ActionContent ClearFix\"]//li/a[text()='%s']";

      String LINKS_OF_SECOND_SPASE = "//div[@class='NormalBox ClearFix FL'][2]//ul[@class='ActionContent ClearFix']//li/a[text()='Join']";

      String TAB_NAME_OF_CREATE_FORM = "//div[@class=\"MiddleTab\" and text()='%s']";

      String OPEN_RADIO_BUTTON = "//table[@class='UIFormGrid']/tbody/tr[2]/td[2]//div/input[1]";

      String MENU_SPACES_LINKS = "//ul[@class='MenuSpace ClearFix']/li/a[text()='%s']";

   }

   @FindBy(linkText = Locators.ADD_NEW_SPACE_LINK)
   private WebElement addSpaceLink;

   @FindBy(id = Locators.NAME_SPACE_FIELD_ID)
   private WebElement nameSpaceField;

   @FindBy(linkText = Locators.CREATE_SPACE_BUTTON)
   private WebElement createBtn;

   @FindBy(xpath = Locators.CREATE_SPACE_BUTTON)
   private WebElement nameFirstWs;

   @FindBy(xpath = Locators.OPEN_RADIO_BUTTON)
   private WebElement openRadioBtn;

   @FindBy(css = Locators.CONTAINER_MESSAGE_APPEAR)
   private WebElement containerMessage;

   /**
    * wait appearance basic elements 
    * from CW page
    * @throws Exception
    */
   public void waitElementsAfterCreateWs() throws Exception
   {
      new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return containerMessage != null && containerMessage.isDisplayed();
         }
      });
   }

   /**
    * wait appearance basic elements 
    * from MyWorkspace page
    * @throws Exception
    */
   public void waitNewSpaceFormAppear() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return createBtn != null && createBtn.isDisplayed() && createBtn != null && createBtn.isDisplayed();
         }
      });
   }

   /**
    * wait appearance radiobtn
    * from Create New Workspace Form
    * @throws Exception
    */
   public void waitOpenSpaseRadoiBtnAppear() throws Exception
   {
      new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return openRadioBtn != null && openRadioBtn.isDisplayed();
         }
      });
   }

   /**
    * wait appearance link of creating WS  
    * @throws Exception
    */
   public void waitLinkinFirstSpaceAppear(final String nameOfFirstWs) throws Exception
   {
      new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            WebElement view = driver.findElement(By.xpath(String.format(Locators.LINKS_OF_FIRST_SPASE, nameOfFirstWs)));
            return view != null && view.isDisplayed();
         }
      });
   }

   /**
    * wait appearance link of creating WS  
    * @throws Exception
    */
   public void waitLinkinSecondSpaceAppear(final String nameOfFirstWs) throws Exception
   {
      new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            WebElement view =
               driver.findElement(By.xpath(String.format(Locators.LINKS_OF_SECOND_SPASE, nameOfFirstWs)));
            return view != null && view.isDisplayed();
         }
      });
   }

   /**
    * wait appearance WS with the specified name 
    * @throws Exception
    */
   public void waitFirstSpaceAppear(final String nameOfFirstWs) throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            WebElement view = driver.findElement(By.xpath(String.format(Locators.NAME_OF_FIRST_SPACE, nameOfFirstWs)));
            return view != null && view.isDisplayed();
         }
      });
   }

   /**
    * wait appearance WS with the specified name 
    * @throws Exception
    */
   public void waitSecondSpaceAppear(final String nameOfFirstWs) throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            WebElement view = driver.findElement(By.xpath(String.format(Locators.NAME_OF_SECOND_SPACE, nameOfFirstWs)));
            return view != null && view.isDisplayed();
         }
      });
   }

   /**
    * wait while create workspace form is closed 
    * 
    */
   public void waitNewSpaceFormDisAppear() throws Exception
   {
      new WebDriverWait(driver, 60).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {
               WebElement view = driver.findElement(By.linkText(Locators.CREATE_SPACE_BUTTON));
               return false;
            }
            catch (Exception e)
            {
               return true;
            }
         }
      });
   }

   /**
    * wait appearance basic elements 
    * from MyWorkspace page
    * @throws Exception
    */
   public void waitAddLink() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {
               return addSpaceLink != null && addSpaceLink.isDisplayed();
            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   /**
    * type name to field
    */
   public void typeSpaceName(String name)
   {
      nameSpaceField.sendKeys(name);
   }

   /**
    * click on add new space button
    */
   public void clickAddNewSpace()
   {
      addSpaceLink.click();
   }

   /**
    * click on add new space button
    */
   public void clickCreateBtn()
   {
      createBtn.click();
   }

   /**
    * get name of first workspace
    */
   public void getNameFirstWs()
   {
      nameFirstWs.getText();
   }

   /**
    * click on tab create New space form
    * @param tabName
    */
   public void clickOnTabCreateWSForm(String tabName)
   {
      WebElement tab = driver.findElement(By.xpath(String.format(Locators.TAB_NAME_OF_CREATE_FORM, tabName)));
      tab.click();
   }

   /**
    * click for select open Ws 
    * @param tabName
    */
   public void clickOnOpenWsRadioBtn()
   {
      openRadioBtn.click();
   }

   /**
    * click for select open Ws 
    * @param tabName
    */
   public void isRadiobtnOpenIsSelect()
   {
      openRadioBtn.isSelected();
   }

   /**
    * click on link in first space with the specified name 
    * @param name
    */
   public void clickOnLinkInFirstSpace(String name)
   {
      driver.findElement(By.xpath(String.format(Locators.LINKS_OF_FIRST_SPASE, name))).click();
   }

   /**
    * click on link in first space with the specified name 
    * @param name
    */
   public void clickOnLinkInSecondSpace(String name)
   {
      driver.findElement(By.xpath(String.format(Locators.LINKS_OF_SECOND_SPASE, name))).click();
   }

   /**
    * click on link in first space with the specified name 
    * @param name
    */
   public void selectInPopUpMenuSpaces(String name)
   {
      driver.findElement(By.xpath(String.format(Locators.MENU_SPACES_LINKS, name))).click();
   }

}
