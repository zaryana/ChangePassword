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
  public void postSave(User newUser, boolean isNew) throws Exception {
    super.postSave(newUser, isNew);
    if(!isNew) return;
    EmailNotificationService notificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
    OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    Event event = new Event(newUser.getUserName(), System.currentTimeMillis());
    ListAccess<User> laUsers = organizationService.getUserHandler().findAllUsers();
    for (User user : laUsers.load(0, laUsers.getSize())) {    
      Set<Event> events = notificationService.getEvents("UserJoinTenantNotificationPlugin", user.getUserName());
      Event eventObj = new Event(event.getIdentity(), event.getCreatedDate());
      if (!events.contains(eventObj)) {
        events.add(eventObj);
      }
      notificationService.setEvents("UserJoinTenantNotificationPlugin", user.getUserName(), events);
    }
  }

}
