package com.exoplatform.cloudworkspaces.platform.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class SearchCalendarTaskTest extends BaseTest {

	static final String TASK_NAME = "TASK_for_TEST";

	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.moveMouseToAppsMenu("Apps");
		CW.WORKSPACE.waitMenuItem("Calendar");
		CW.WORKSPACE.clickOnMenuItem("Calendar");
		CW.CALENDAR.waitWeekTable();
		SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
		Date now = new Date();
		String date = format.format(now).replace(' ', '/');

		CW.CALENDAR.clickOnTaskIcon();
		CW.CALENDAR.waitTaskForm();
		CW.CALENDAR.typeToTaskField(TASK_NAME);
		CW.CALENDAR.typeToNoteTaskField(TASK_NAME);
		CW.CALENDAR.typeTaskFromDateField(date + "\n");
		CW.CALENDAR.typeSetFromTimeTaskField("11:00\n");
		CW.CALENDAR.typeSetToTimeTaskField("23:00\n");
		CW.CALENDAR.taskSaveClick();
		CW.CALENDAR.waitTaskOrEventAppearence(TASK_NAME);

	}

	@Test
	public void searchTask() throws Exception {
		CW.CALENDAR.typeInSearchField(TASK_NAME + "\n");
		CW.CALENDAR.checkSearchResult(TASK_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.CALENDAR.clickOnTopCalendarButton("Week");
		CW.CALENDAR.waitWeekTable();
		CW.CALENDAR.waitTaskOrEventAppearence(TASK_NAME);
		CW.CALENDAR.eventContexMenu(TASK_NAME);
		CW.CALENDAR.deleteCalendarEvent();
		driver.switchTo().alert().accept();
		CW.CALENDAR.verifyThatEventDeleted(TASK_NAME);
	}
}
