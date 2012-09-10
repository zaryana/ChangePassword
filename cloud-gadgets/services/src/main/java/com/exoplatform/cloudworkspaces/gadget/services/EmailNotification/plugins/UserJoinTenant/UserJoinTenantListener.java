package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationService;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;

public class UserJoinTenantListener extends UserEventListener{

  @Override
  public void postSave(User user, boolean isNew) throws Exception {
    super.postSave(user, isNew);
    if(!isNew) return;
    EmailNotificationService notificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);

    ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();
    for (User u : laUsers.load(0, laUsers.getSize())) {		
      Set<Event> events = notificationService.getEvents(this.getName(), user.getUserName());
      Event event = new Event(u.getUserName(), System.currentTimeMillis());
      if (!events.contains(event)) {
        events.add(event);
      }
      notificationService.setEvents(this.getName(), user.getUserName(), events);
    }
  }

}
