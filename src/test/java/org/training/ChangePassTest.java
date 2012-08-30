package org.training;

import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.NoSuchElementException;

public class ChangePassTest
{

   WebDriver driver;

   @Before
   public void startDriver()
   {
      driver = new FirefoxDriver();
   }

   @After
   public void stopDriver()
   {
      driver.close();
   }

   @Test
   public void test()
   {
      driver.get("http://exoplatform.cloud-workspaces.com/");

      (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>()
      {
         public Boolean apply(WebDriver d)
         {
            try
            {
               return d.findElement(By.name("signinForm")).isDisplayed();

            }
            catch (NoSuchElementException e)
            {
               return false;
            }
            
         }
      });

             
     driver.findElement(By.linkText("Forgot Password?")).click();
    
      (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>()
      {
         public Boolean apply(WebDriver d)
         {
            try
            {
               return d.findElement(By.id("resetForm")).isDisplayed();
       
            }
            catch (NoSuchElementException e)
            {
               return false;
            }
         }
      });
      
  	   driver.findElement(By.id("email")).clear();
      driver.findElement(By.id("email")).sendKeys("zdombrovskaya@exoplatform.com");
      driver.findElement(By.id("submitButton")).click();

      (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>()
      {
         public Boolean apply(WebDriver d)
         
         
         {
            try
            {
               return d.findElement(By.id("messageString")).isDisplayed();
               
            }
            catch (NoSuchElementException e)
            {
               return false;
            }
           
         }
         
      });
      
      WebElement emailField = driver.findElement(By.id("email"));
		
				emailField.clear();
				assertTrue(emailField.getAttribute("value").isEmpty());
		
				emailField.sendKeys("zdombrovskaya@exoplatform.com");
				assertEquals("zdombrovskaya@exoplatform.com", emailField.getAttribute("value"));
			  
           
      (new WebDriverWait(driver, 10)).until(ExpectedConditions.invisibilityOfElementLocated(By.id("submitButton")));
      
      WebElement message = driver.findElement(By.id("messageString"));
	  assertEquals("Request completed, check your email for instructions.", message.getText()); 
     
   }

}
