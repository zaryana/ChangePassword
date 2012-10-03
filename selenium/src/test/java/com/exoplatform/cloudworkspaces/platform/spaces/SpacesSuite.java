package com.exoplatform.cloudworkspaces.platform.spaces;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CreateOpenSpaceTest.class, CreateSpaceWithValidationTest.class,
		DeleteSpaceTest.class, JoinSpaceWithValidationTest.class,
		JointToOpenSpaceTest.class, LeaveOpenSpaceTest.class,
		LeaveSpaceWithValidationTest.class })
public class SpacesSuite {
}
