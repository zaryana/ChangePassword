package com.exoplatform.cloudworkspaces.platform.spaces;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class JoinSpaceWithValidationTest extends BaseTest {

	static final String SPACE_NAME = "JVALID";

	@Before
	public void doPrepare() throws Exception {
		CW.SPACES.createSpaceWithValidation(SPACE_NAME);
	}

	@Test
	public void joinSpaceWithValidation() throws Exception {
		logout();
		loginAsSecondUser();
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Request to Join");
		CW.SPACES.clickOnSpaceButton(SPACE_NAME, "Request to Join");
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Cancel");
		logout();
		loginAsFirstUser();
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Edit");
		CW.SPACES.clickOnSpaceButton(SPACE_NAME, "Edit");
		CW.SPACE_SETTINGS.waitSettingsMenu();
		CW.SPACE_SETTINGS.clickOnSpecifiedSettingsButton("Members");
		CW.SPACE_SETTINGS.waitAddMemberForm();
		CW.SPACE_SETTINGS.waitValidateInvitationButton();
		CW.SPACE_SETTINGS.clickOnValidateInvitationButton();
		CW.SPACE_SETTINGS.waitValidateInvitationButtonDisappear();
		CW.SPACE_SETTINGS.wainGrantManagerButton();
		logout();
		loginAsSecondUser();
		CW.TOPMENUS.moveMouseToTopSubMenu("Browse");
		CW.TOPMENUS.waitSubLink("Spaces");
		CW.TOPMENUS.clickOnSublink("Spaces");
		CW.SPACES.waitAddLink();
		CW.SPACES.checkThatSpaceButtonIsAppeared(SPACE_NAME, "Leave");
		CW.SPACES.clickOnSpaceNameToEnterSpace(SPACE_NAME);
		CW.WORKSPACE.waitMessage(1);
		assertEquals(CW.WORKSPACE.getTitlemess(1), SPACE_NAME);
	}

	@After
	public void cleanUp() throws Exception {
		logout();
		loginAsFirstUser();
		CW.SPACES.deleteSpace(SPACE_NAME);
	}
}
