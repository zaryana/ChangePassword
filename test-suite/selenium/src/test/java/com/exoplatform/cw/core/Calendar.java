package com.exoplatform.cw.core;

import com.exoplatform.cw.BaseTest;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Calendar extends BaseTest
{
   private interface Locators
   {

      String SELECT_WORKSPACE_LINK = "%s";

      String WEEK_TABLE_CLASS = "table#UIWeekViewGrid>tbody";

      String EVENT_FORM_TITLE_CLASS = "PopupTitle";

      String EVENT_FORM_SUM_ID = "eventName";

      String EVENT_FORM_DESC_ID = "description";

      String TASK_FORM_PREFIX = "//div[@id='UIQuckAddTaskPopupWindow']";

      String TASK_FORM_EVENT = "//div[@id='UIQuckAddTaskPopupWindow']//input[@id='eventName']";

      String TASK_FORM_NOTE = "//div[@id='UIQuckAddTaskPopupWindow']//td[@class=\"FieldComponent\"]/textarea";

      String TASK_FORM_SET_FROM_DATE = TASK_FORM_PREFIX + "//div[@class='Date']/input";

      String TASK_FORM_SET_FROM_TIME =
         "//div[@id='UIQuckAddTaskPopupWindow']//div[@class=\"UIComboboxComponent\"]/input[@class='UIComboboxInput']";

      String TASK_FORM_SET_TO_DATE =
         "//div[@id='UIQuckAddTaskPopupWindow']//table/tbody/tr[4]//div[@class='Date']/input";

      String TASK_FORM_SET_TO_TIME =
         "//div[@id='UIQuckAddTaskPopupWindow']//table/tbody/tr[4]//div[@class='Time']//input[@class='UIComboboxInput']";

      String TASK_SAVE_BTN = TASK_FORM_PREFIX + "//a[text()='Save']";

      String TASK_CANCEL_BTN = TASK_FORM_PREFIX + "//a[text()='Cancel']";

      String TASK_MORE_DETAILS_BTN = TASK_FORM_PREFIX + "//a[text()='More Details']";

      String SET_FROM_DATE = "//table[@class='UIFormGrid']/tbody/tr[3]/td[2]/div[1]/input";

      String SET_FROM_TIME = "//table[@class='UIFormGrid']/tbody/tr[3]/td[2]/div[2]//input[@class='UIComboboxInput']";

      String SET_TO_DATE = "//table[@class='UIFormGrid']/tbody/tr[4]/td[2]/div[1]/input";

      String SET_TO_TIME = "//table[@class='UIFormGrid']/tbody/tr[4]/td[2]/div[2]//input[@class='UIComboboxInput']";

      String ALLDAY_CHECK_BOX = "input[name=allDay]";

      String EVENT_FORM_SELECT_CALENDAR = "td.FieldComponent>select.selectbox";

      String TASK_FORM_CLICK_CALENDAR_COMBOBOX = TASK_FORM_PREFIX + "//select[@name=\"calendar\"]";

      String EVENT_SELECT_WS_CALENDAR = "//td[@class='FieldComponent']/select[@class='selectbox']/optgroup[@label='Group Calendars']/option[contains(text(), '%s')]";

      String TASK_SELECT_WS_CALENDAR = TASK_FORM_PREFIX + EVENT_SELECT_WS_CALENDAR;

      String EVENT_FORM_CATEGORY_DEPLOY = "";

      String EVENT_FORM_CATEGORY_SELECT = "";

      String SAVE_BTN = "Save";

      String MORE_DETAILS_BTN = "More Details";

      String CANCEL_BTN = "Cancel";

      String WEEKLY_TABLE_NAVIGATION = "//table[@id='UIWeekViewGrid']/tbody/tr[%s]/td[%s]";

      String EVENT_CONTAINER = "//div[@class='EventContainer'and text()='%s']";

      String TASK_CONTAINER = "";

      String TASK_ICON_CLASS = "//a[@class='IconHolder AddNewTask']";
   }
   
   @FindBy(css = Locators.WEEK_TABLE_CLASS)
   private WebElement weekTable;

   @FindBy(className = Locators.EVENT_FORM_TITLE_CLASS)
   private WebElement labelEventForm;

   @FindBy(id = Locators.EVENT_FORM_SUM_ID)
   private WebElement eventSummaryField;

   @FindBy(id = Locators.EVENT_FORM_DESC_ID)
   private WebElement descriptionField;

   @FindBy(xpath = Locators.SET_FROM_DATE)
   private WebElement eventFromDateField;

   @FindBy(xpath = Locators.SET_TO_DATE)
   private WebElement eventToDateField;

   @FindBy(linkText = Locators.SAVE_BTN)
   private WebElement saveBtn;

   @FindBy(linkText = Locators.MORE_DETAILS_BTN)
   private WebElement moreDetBtn;

   @FindBy(linkText = Locators.MORE_DETAILS_BTN)
   private WebElement cancelBtn;

   @FindBy(xpath = Locators.SET_TO_TIME)
   private WebElement setToTime;

   @FindBy(xpath = Locators.TASK_ICON_CLASS)
   private WebElement taskIcon;

   @FindBy(xpath = Locators.SET_FROM_TIME)
   private WebElement setFromTime;

   @FindBy(xpath = Locators.TASK_FORM_PREFIX)
   private WebElement taskForm;

   @FindBy(xpath = Locators.TASK_FORM_EVENT)
   private WebElement taskEventField;

   @FindBy(xpath = Locators.TASK_FORM_NOTE)
   private WebElement taskNoteField;

   @FindBy(xpath = Locators.TASK_FORM_SET_FROM_DATE)
   private WebElement taskSetFromDate;

   @FindBy(xpath = Locators.TASK_FORM_SET_FROM_TIME)
   private WebElement taskSetFromTime;

   @FindBy(xpath = Locators.TASK_FORM_SET_TO_DATE)
   private WebElement taskSetToDate;

   @FindBy(xpath = Locators.TASK_FORM_SET_TO_TIME)
   private WebElement taskSetToTime;

   @FindBy(xpath = Locators.TASK_SAVE_BTN)
   private WebElement taskSaveBtn;

   @FindBy(xpath = Locators.TASK_CANCEL_BTN)
   private WebElement taskCancelBtn;

   @FindBy(css = Locators.EVENT_FORM_SELECT_CALENDAR)
   private WebElement selectBoxEvent;

   @FindBy(xpath = Locators.TASK_FORM_CLICK_CALENDAR_COMBOBOX)
   private WebElement taskFormCombobox;

   /**
    * @param numMess
    * @throws Exception
    *  wait appearance table
    *  from Calendar
    */
   public void waitWeekTable() throws Exception
   {
      new WebDriverWait(driver, 30).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return weekTable != null && weekTable.isDisplayed();
         }
      });
   }

   /**
    * wait appearence event on 
    * Weekly table
    * @param titleEvent
    */
   public void waitEventAppearence(final String titleEvent)
   {

      new WebDriverWait(driver, 60).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            WebElement event = driver.findElement(By.xpath(String.format(Locators.EVENT_CONTAINER, titleEvent)));
            return event != null && event.isDisplayed();
         }
      });
   }

   
   /**
    * wait disappear event in 
    * Weekly table
    * @param titleEvent
    */
   public void waitEventOpenspaceDisAppearence(final String titleEvent)
   {

      new WebDriverWait(driver, 5).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
               WebElement event = driver.findElement(By.xpath(String.format(Locators.EVENT_CONTAINER, titleEvent)));
               return !(event !=null && event.isDisplayed());
         }
      });
   }

   /**
    * wait basic elements of
    * event form
    * @throws Exception
    */
   public void waitEventForm() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return saveBtn != null && saveBtn.isDisplayed() && moreDetBtn != null && moreDetBtn.isDisplayed()
               && cancelBtn != null && cancelBtn.isDisplayed() && eventSummaryField != null
               && eventSummaryField.isDisplayed() && eventFromDateField != null && eventFromDateField.isDisplayed();
         }
      });
   }

   /**
    * wait basic elements of
    * task form
    * @throws Exception
    */
   public void waitTaskForm() throws Exception
   {
      new WebDriverWait(driver, 10).until(new ExpectedCondition<Boolean>()
      {
         @Override
         public Boolean apply(WebDriver elem)
         {
            return taskForm != null && taskForm.isDisplayed() && taskSaveBtn != null && taskSaveBtn.isDisplayed()
               && taskEventField != null && taskEventField.isDisplayed();
         }
      });
   }

   /**
    * click on Weekly table
    * 
    * @param col start with 1
    * @param row start with 1
    */
   public void clickOnWeeklyTable(int col, int row)
   {
      driver.findElement(By.xpath(String.format(Locators.WEEKLY_TABLE_NAVIGATION, col, row))).click();
   }

   /**
    * type to summary field
    * of event form
    */
   public void typeToSummaryField(String event)
   {
      eventSummaryField.sendKeys(event);
   }

   /**
    * type to task field
    * of event form
    */
   public void typeToNoteTaskField(String task)
   {
      taskNoteField.sendKeys(task);
   }

   /**
    * type to task field
    * of event form
    */
   public void typeTaskFromDateField(String task)
   {
      taskSetFromDate.clear();
      taskSetFromDate.sendKeys(task);
   }

   /**
    * type to task From time field
    * of event form
    */
   public void typeSetFromTimeTaskField(String task)
   {
      taskSetFromTime.clear();
      taskSetFromTime.sendKeys(task);
   }

   /**
    * type to task To time field
    * of event form
    */
   public void typeSetToTimeTaskField(String task)
   {
      taskSetToTime.clear();
      taskSetToTime.sendKeys(task);
   }

   /**
    * type to summary field
    * of event form
    */
   public void typeToTaskField(String task)
   {
      taskEventField.sendKeys(task);
   }

   /**
    * type to description field
    * of event form
    * @param description
    */
   public void typeToDescriptionField(String description)
   {
      descriptionField.sendKeys(description);
   }

   /**
    * set time in "set from time field"
    * of event form
    * @param time
    */
   public void typeSetFromTime(String time)
   {
      setFromTime.clear();
      setFromTime.sendKeys(time);
   }

   /**
    * set date in "set from date field"
    * of event form
    * @param time
    */
   public void typeFromDateTimeEventField(String date)
   {
      eventFromDateField.clear();
      eventFromDateField.sendKeys(date);
   }

   /**
    * set date in "set from date field"
    * of event form
    * @param time
    */
   public void typeToDateTimeEventField(String date)
   {
      eventToDateField.clear();
      eventToDateField.sendKeys(date);
   }

   /**
    * set time in "set to time field"
    * of event form
    * @param time
    */
   public void typeSetToTime(String time)
   {
      setToTime.clear();
      setToTime.sendKeys(time);
   }

   /**
    * click on save 
    * button of event form
    *
    *   */
   public void saveBtnClick()
   {
      saveBtn.click();
   }

   /**
    * click on task icon 
    *   */
   public void clickOnTaskIcon()
   {
      taskIcon.click();
   }

   /**
    * click on task save btn
    *   */
   public void taskSaveClick()
   {
      taskSaveBtn.click();
   }

   /**
    * click on task calendar combobox
    *   */
   public void selectBoxTaskClick()
   {
      taskFormCombobox.click();
   }

   /**
    * click on calendar combobox
    *   */
   public void selectBoxEventClick()
   {
      selectBoxEvent.click();
   }

   /**
    * select WS in calendar combobox
    * @throws InterruptedException 
    *   */
   public void selectWorkSpaceEventForm(String name) throws InterruptedException
   {
     
   WebElement elem = driver.findElement(By.xpath(String.format(Locators.EVENT_SELECT_WS_CALENDAR, name)));
   new Actions(driver).doubleClick(elem).build().perform();
   
   
    // driver.findElement(By.xpath(String.format(Locators.EVENT_SELECT_WS_CALENDAR, name))).click();
   }

   /**
    * select WS in calendar combobox
    *   */
   public void selectWorkSpaceTaskForm(String name)
   {
      WebElement elem = driver.findElement(By.xpath(String.format(Locators.TASK_SELECT_WS_CALENDAR, name)));
      new Actions(driver).doubleClick(elem).build().perform();
   }

   /**
    * click on checkbox for select workspace
    * in calendar
    *   
    *   */
   public void clicOnCheckBoxSelecWs(String name)
   {
      driver.findElement(By.partialLinkText(String.format(Locators.SELECT_WORKSPACE_LINK, name))).click();
   }

}
