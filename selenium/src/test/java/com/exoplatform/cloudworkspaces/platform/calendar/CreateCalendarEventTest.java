package com.exoplatform.cloudworkspaces.platform.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class CreateCalendarEventTest extends BaseTest {

	static final String EVENT_NAME = "Event_for_TEST";

	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.moveMouseToAppsMenu("Apps");
		CW.WORKSPACE.waitMenuItem("Calendar");
		CW.WORKSPACE.clickOnMenuItem("Calendar");
		CW.CALENDAR.waitWeekTable();
	}

	@Test
	public void createEvent() throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("MM dd yyyy");
		Date now = new Date();
		String date = format.format(now).replace(' ', '/');

		CW.CALENDAR.clickOnWeeklyTable(19, 2);
		CW.CALENDAR.waitEventForm();
		CW.CALENDAR.typeToSummaryField(EVENT_NAME);
		CW.CALENDAR.typeToDescriptionField("Test_event_for_Default_Ws");
		CW.CALENDAR.typeFromDateTimeEventField(date + "\n");
		CW.CALENDAR.typeToDateTimeEventField(date + "\n");
		CW.CALENDAR.typeSetToTime("23:00\n");
		CW.CALENDAR.saveBtnClick();
		CW.CALENDAR.waitTaskOrEventAppearence(EVENT_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		CW.CALENDAR.eventContexMenu(EVENT_NAME);
		CW.CALENDAR.deleteCalendarEvent();
		driver.switchTo().alert().accept();
		CW.CALENDAR.verifyThatEventDeleted(EVENT_NAME);
	}
}
