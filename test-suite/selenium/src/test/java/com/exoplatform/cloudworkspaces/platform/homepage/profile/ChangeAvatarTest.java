package com.exoplatform.cloudworkspaces.platform.homepage.profile;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.exoplatform.cw.BaseTest;

public class ChangeAvatarTest extends BaseTest {

	static final File file = new File(
			"src/test/resources/org/exoplatform/cw/avatar/avatar.jpg");
	static final String PATH_TO_AVATAR_ICON = file.getAbsolutePath();
	
	@Before
	public void doPrepare() throws Exception {
		CW.WORKSPACE.waitBasicElements();
		CW.WORKSPACE.selectMyProfileIframe();
		CW.WORKSPACE.waitNewLinkAppear("Edit My Profile");
		CW.WORKSPACE.clickOnMyProfileLink();
	}
	
	@Test
	public void changeAvatar() throws Exception {
		CW.PROFILE.waitChangeAvatarLink();
		CW.PROFILE.clickChangeAvatar();
		CW.PROFILE.waitBottomLoadForm();
		CW.PROFILE.switchToUploadIframe();
		CW.PROFILE.typeToLoadFileInput(PATH_TO_AVATAR_ICON);
		selectMainFrame();
		CW.PROFILE.waitBottomLoadForm();
		CW.PROFILE.confirmClick();
		CW.PROFILE.waitConfirmSaveAvatarForm();
		CW.PROFILE.clickSaveAvatar();
	}


}
