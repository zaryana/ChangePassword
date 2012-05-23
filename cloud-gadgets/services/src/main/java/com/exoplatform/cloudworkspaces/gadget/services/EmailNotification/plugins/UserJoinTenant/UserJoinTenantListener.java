package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

public class UserJoinTenantListener extends UserEventListener{

	@Override
	public void postSave(User user, boolean isNew) throws Exception {
		super.postSave(user, isNew);
		if(!isNew) return;
		UserJoinTenantLog joinLog = (UserJoinTenantLog) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserJoinTenantLog.class);
		joinLog.addEntry(user.getUserName(), System.currentTimeMillis());
	}

}
