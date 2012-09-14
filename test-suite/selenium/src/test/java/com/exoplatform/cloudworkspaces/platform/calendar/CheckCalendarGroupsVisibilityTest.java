package com.exoplatform.cloudworkspaces.platform.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CheckCalendarGroupsVisibilityTest extends BaseTest {

	static final String EVENT_NAME = "THIS_IS_EVENT";
	static final String TASK_NAME = "THIS_IS_TASK";
	static final String SPACE_NAME = "OSPACE";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createOpenSpace(SPACE_NAME);
	}

	@Test
	public void checkCalendarVisibility() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
		Date now = new Date();
		String date = format.format(now).replace(' ', '/');

		CW.WORKSPACE.goToHome();
		CW.WORKSPACE.waitBasicElements();
		CW.WORKSPACE.moveMouseToAppsMenu("Apps");
		CW.WORKSPACE.waitMenuItem("Calendar");
		CW.WORKSPACE.clickOnMenuItem("Calendar");
		CW.CALENDAR.waitWeekTable();

		CW.CALENDAR.clickOnWeeklyTable(19, 2);
		CW.CALENDAR.waitEventForm();
		CW.CALENDAR.typeToSummaryField(EVENT_NAME);
		CW.CALENDAR.typeToDescriptionField(EVENT_NAME);
		CW.CALENDAR.typeFromDateTimeEventField(date + "\n");
		CW.CALENDAR.typeToDateTimeEventField(date + "\n");
		CW.CALENDAR.typeSetToTime("20:00\n");
		CW.CALENDAR.selectBoxEventClick();
		CW.CALENDAR.selectWorkSpaceEventForm(SPACE_NAME);
		CW.CALENDAR.saveBtnClick();
		CW.CALENDAR.waitTaskOrEventAppearence(EVENT_NAME);

		CW.CALENDAR.clickOnTaskIcon();
		CW.CALENDAR.waitTaskForm();
		CW.CALENDAR.typeToTaskField(TASK_NAME);
		CW.CALENDAR.typeToNoteTaskField(TASK_NAME);
		CW.CALENDAR.typeTaskFromDateField(date + "\n");
		CW.CALENDAR.typeSetFromTimeTaskField("21:00\n");
		CW.CALENDAR.typeSetToTimeTaskField("23:00\n");
		CW.CALENDAR.selectBoxTaskClick();
		CW.CALENDAR.selectWorkSpaceTaskForm(SPACE_NAME);
		CW.CALENDAR.taskSaveClick();
		CW.CALENDAR.waitTaskOrEventAppearence(TASK_NAME);

		CW.CALENDAR.clicOnCheckBoxSelecWs(SPACE_NAME);
		CW.CALENDAR.waitTaskOrEventDisappearing(EVENT_NAME);
		CW.CALENDAR.waitTaskOrEventDisappearing(TASK_NAME);
		CW.CALENDAR.clicOnCheckBoxSelecWs(SPACE_NAME);
		CW.CALENDAR.waitTaskOrEventAppearence(TASK_NAME);
		CW.CALENDAR.waitTaskOrEventAppearence(EVENT_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.CALENDAR.eventContexMenu(EVENT_NAME);
		CW.CALENDAR.deleteCalendarEvent();
		driver.switchTo().alert().accept();
		CW.CALENDAR.verifyThatEventDeleted(EVENT_NAME);
		CW.CALENDAR.eventContexMenu(TASK_NAME);
		CW.CALENDAR.deleteCalendarEvent();
		driver.switchTo().alert().accept();
		CW.CALENDAR.verifyThatEventDeleted(TASK_NAME);
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
