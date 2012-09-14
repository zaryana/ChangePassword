package com.exoplatform.cloudworkspaces.platform.forum;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PostTopicTest.class, ReplyForumPostTest.class,
		ReplyPrivateForumPostTest.class, SearchForumTest.class })
public class ForumSuite {
}
