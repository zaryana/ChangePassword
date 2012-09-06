package com.exoplatform.cw.core;

import com.exoplatform.cw.BaseTest;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FooterUIElements extends BaseTest
{

   private interface Locators
   {
      String UI_FOOTER_ID = "UIFooter";

      String FOOTER_BY_LABEL = "//p[@class=\"FL\"and text()='Cloud Workspaces is Brought to You by ']";

      String FOOTER_COPYRIGHT_LABEL =
         "//p[@class=\"Copyright FR\"and text()='Copyright © 2000-2012. All Rights Reserved, eXo Platform SAS.']";
   }

   @FindBy(id = Locators.UI_FOOTER_ID)
   private WebElement footerContainer;

   @FindBy(xpath = Locators.FOOTER_BY_LABEL)
   private WebElement brouthToYouBy;

   @FindBy(xpath = Locators.FOOTER_COPYRIGHT_LABEL)
   private WebElement copyrightInfo;

   /**
    * wait base elements of sign form
    * @throws Exception
    */
   public void waitFooterElements() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {

            return footerContainer != null && footerContainer.isDisplayed() && brouthToYouBy != null
               && brouthToYouBy.isDisplayed() && copyrightInfo != null && copyrightInfo.isDisplayed();

         }
      });
   }

}
