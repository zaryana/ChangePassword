package com.exoplatform.cloudworkspaces.platform.calendar;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateCalendarEventTest.class, CreateCalendarTaskTest.class,
		CheckCalendarGroupsVisibilityTest.class, SearchCalendarTaskTest.class })
public class CalendarSuite {
}
