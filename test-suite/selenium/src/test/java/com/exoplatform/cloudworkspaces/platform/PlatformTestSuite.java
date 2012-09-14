package com.exoplatform.cloudworkspaces.platform;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.exoplatform.cloudworkspaces.platform.calendar.CalendarSuite;
import com.exoplatform.cloudworkspaces.platform.documents.DocumentsSuite;
import com.exoplatform.cloudworkspaces.platform.forum.ForumSuite;
import com.exoplatform.cloudworkspaces.platform.homepage.activitystream.ActivitiesSuite;
import com.exoplatform.cloudworkspaces.platform.homepage.profile.ProfileSuite;
import com.exoplatform.cloudworkspaces.platform.spaces.SpacesSuite;
import com.exoplatform.cloudworkspaces.platform.wiki.WikiSuite;

@RunWith(Suite.class)
@SuiteClasses({ ActivitiesSuite.class, ProfileSuite.class, SpacesSuite.class, CalendarSuite.class,
		ForumSuite.class, DocumentsSuite.class, WikiSuite.class })
public class PlatformTestSuite {
}