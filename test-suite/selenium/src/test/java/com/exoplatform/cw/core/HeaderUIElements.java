package com.exoplatform.cw.core;

import com.exoplatform.cw.BaseTest;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HeaderUIElements extends BaseTest
{

   private interface Locators
   {
      String UI_HEADER_ID = "UIHeader";

      String HEADER_LINKS_CLASS = "MainContent";

      String EXO_LOGO_LOCATOR = "//img[@src='/background/logo.png']";

      String ABOUT_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='About']";

      String FORUM_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Forum'] ";

      //for old versions
      //String HELP = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Help'] ";
      
      String HELP = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='FAQ'] ";

      String EXO_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='eXoplatform.com']";

      String CONTACT_AS_LINK = "//ul[@class=\"UIMenuTop FR\"]//a[@class and text()='Contact us']";

      String HEADER_CLOUD_WORKSPACES = "div.ClouIntranet>h1";

      String SPAN_CLOUD_WORKSPACES = "div.ClouIntranet>span";

   }

   //WebElemnts Base Header Elements
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

   /**
    * wait base elements of sign form
    * @throws Exception
    */
   public void waitHeaderElements() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            try
            {
               return headerContainer != null && headerContainer.isDisplayed() && hederMainLinksContainer != null
                  && hederMainLinksContainer.isDisplayed() && exoLogo != null && exoLogo.isDisplayed() && about != null
                  && about.isDisplayed() && forum != null && forum.isDisplayed() && help != null && help.isDisplayed()
                  && mainLink != null && mainLink.isDisplayed() && contactAs != null && contactAs.isDisplayed();
            }
            catch (Exception e)
            {
               return false;
            }
         }
      });
   }

   public String getHeader()
   {
      String header = firstHeader.getText() + "\n" + secondHeader.getText();
      return header;
   }

}
