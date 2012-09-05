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
package com.exoplatform.cloudworkspaces.platform;

import com.exoplatform.cw.BaseTest;

import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:mmusienko@exoplatform.com">Musienko Maxim</a>
 * @version $Id:
 *
 */
public class CheckCalendarTest extends BaseTest
{
  // static final String DEFAULT_WORKSPASE = "Default";

 //  static final String OPEN_WORKSPASE = "OpenSpace";

   @BeforeClass
   public static void testSignUpToWS() throws Exception
   {
      //wait appearance login page and login 
      CW.LOGIN_PAGE.waitAppearLoginPageStaging();
      CW.LOGIN_PAGE.typeUserName();
      CW.LOGIN_PAGE.typeUserPass();
      CW.LOGIN_PAGE.clickOnLoginBtnStg();
   }

   @Test
   public void calendarTest() throws Exception
   {
      SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
      Date now = new Date();
      String date = format.format(now).replace(' ', '/');
      
      CW.WORKSPACE.waitBasicElements();
      CW.WORKSPACE.clickOnCalendar();
      CW.CALENDAR.waitWeekTable();
      CW.CALENDAR.clickOnWeeklyTable(19, 2);
      CW.CALENDAR.waitEventForm();

      CW.CALENDAR.typeToSummaryField("Event_for_default_WS");
      CW.CALENDAR.typeToDescriptionField("Test_event_for_Default_Ws");
      CW.CALENDAR.typeFromDateTimeEventField(date + "\n");
      CW.CALENDAR.typeToDateTimeEventField(date + "\n");
      CW.CALENDAR.typeSetToTime("10:00\n");
      CW.CALENDAR.selectBoxEventClick();
      CW.CALENDAR.selectWorkSpaceEventForm("SeleniumDefault");
      CW.CALENDAR.saveBtnClick();
      CW.CALENDAR.waitEventAppearence("Event_for_default_WS");

      CW.CALENDAR.clickOnTaskIcon();
      CW.CALENDAR.waitTaskForm();
      CW.CALENDAR.typeToTaskField("Task_for_default_WS");
      CW.CALENDAR.typeToNoteTaskField("Task_event_for_Default_Ws");
      CW.CALENDAR.typeTaskFromDateField(date + "\n");
      CW.CALENDAR.typeSetFromTimeTaskField("11:00\n");
      CW.CALENDAR.typeSetToTimeTaskField("13:00\n");
      CW.CALENDAR.selectBoxTaskClick();
      CW.CALENDAR.selectWorkSpaceTaskForm("SeleniumDefault");
      CW.CALENDAR.taskSaveClick();
      CW.CALENDAR.waitEventAppearence("Task_for_default_WS");

      CW.CALENDAR.clickOnWeeklyTable(19, 2);
      CW.CALENDAR.waitEventForm();
      CW.CALENDAR.typeToSummaryField("Event_for_open_WS");
      CW.CALENDAR.typeToDescriptionField("Test_event_for_open_Ws");
      CW.CALENDAR.typeFromDateTimeEventField(date + "\n");
      CW.CALENDAR.typeToDateTimeEventField(date + "\n");
      CW.CALENDAR.typeSetFromTime("14:00\n");
      CW.CALENDAR.typeSetToTime("15:00\n");
      CW.CALENDAR.selectBoxEventClick();
      CW.CALENDAR.selectWorkSpaceEventForm("SeleniumOpen");
      CW.CALENDAR.saveBtnClick();
      CW.CALENDAR.waitEventAppearence("Event_for_open_WS");

      CW.CALENDAR.clickOnTaskIcon();
      CW.CALENDAR.waitTaskForm();
      CW.CALENDAR.typeToTaskField("Task_for_open_WS");
      CW.CALENDAR.typeToNoteTaskField("Task_event_for_open_Ws");
      CW.CALENDAR.typeSetFromTimeTaskField("16:00\n");
      CW.CALENDAR.typeSetToTimeTaskField("18:00\n");
      CW.CALENDAR.selectBoxTaskClick();
      CW.CALENDAR.selectWorkSpaceTaskForm("SeleniumOpen");
      CW.CALENDAR.taskSaveClick();
      CW.CALENDAR.waitEventAppearence("Task_for_open_WS");
    
      CW.CALENDAR.clicOnCheckBoxSelecWs("SeleniumOpen");
      CW.CALENDAR.waitEventOpenspaceDisAppearence("Task_for_open_WS");
      CW.CALENDAR.waitEventOpenspaceDisAppearence("Event_for_open_WS");

      CW.CALENDAR.clicOnCheckBoxSelecWs("SeleniumDefault");
      CW.CALENDAR.waitEventOpenspaceDisAppearence("Event_for_default_WS");
      CW.CALENDAR.waitEventOpenspaceDisAppearence("Task_for_default_WS");
      
      CW.CALENDAR.clicOnCheckBoxSelecWs("SeleniumOpen");
      CW.CALENDAR.clicOnCheckBoxSelecWs("SeleniumDefault");
      CW.CALENDAR.waitEventAppearence("Task_for_default_WS");
      CW.CALENDAR.waitEventAppearence("Event_for_default_WS");
      CW.CALENDAR.waitEventAppearence("Task_for_open_WS");
      CW.CALENDAR.waitEventAppearence("Event_for_open_WS");
   }

}
