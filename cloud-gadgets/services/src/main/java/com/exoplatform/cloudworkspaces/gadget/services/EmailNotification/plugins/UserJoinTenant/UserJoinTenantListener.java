package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import java.util.Set;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationStorage;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Plugin;

public class UserJoinTenantListener extends UserEventListener{

	@Override
	public void postSave(User user, boolean isNew) throws Exception {
		super.postSave(user, isNew);
		if(!isNew) return;
		EmailNotificationStorage notificationStorage = (EmailNotificationStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationStorage.class);      
    Set<Event> events = notificationStorage.getEvents(Plugin.USER_JOIN_TENANT, user.getUserName());
    Event event = new Event(user.getUserName(), System.currentTimeMillis());
    if (!events.contains(event)) {
      events.add(event);
    }
	}

}
