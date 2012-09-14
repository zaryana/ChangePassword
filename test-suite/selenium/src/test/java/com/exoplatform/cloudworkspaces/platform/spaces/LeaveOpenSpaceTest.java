package com.exoplatform.cloudworkspaces.platform.spaces;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class LeaveOpenSpaceTest extends BaseTest {

	static final String SPACE_NAME = "LOPEN";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createOpenSpace(SPACE_NAME);
	}

	@Test
	public void leaveFromOpenSpace() throws Exception {
		logout();
		loginAsSecondUser();
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Join");
		CW.SPACES.clickOnSpaceButton(SPACE_NAME, "Join");
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Leave");
		CW.SPACES.clickOnSpaceNameToEnterSpace(SPACE_NAME);
		CW.WORKSPACE.waitMessage(1);
		assertEquals(CW.WORKSPACE.getTitlemess(1), SPACE_NAME);
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Leave");
		CW.SPACES.clickOnSpaceButton(SPACE_NAME, "Leave");
		CW.SPACES.checkThatSpaceButtonIsDisappeared(SPACE_NAME, "Leave");
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Join");
	}

	@After
	public void cleanUp() throws Exception {
		logout();
		loginAsFirstUser();
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
