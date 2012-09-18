package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.DateTimeUtils;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationService;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

public class SpaceNotificationPlugin extends EmailNotificationPlugin{
  private static Log LOG = ExoLogger.getLogger(SpaceNotificationPlugin.class);
  
  @Override
  public String exec(Map<String, Object> context) {
    try {
      String userId = (String)context.get("userId");
      MessagesCache messagesCache = (MessagesCache)context.get("pluginMessagesCache");
      Properties messages = messagesCache.get((String) context.get("userLocale"));
      long lastRun = (Long)context.get("lastRun");
      boolean isSummaryMail = (Boolean) context.get("isSummaryMail");

      IdentityManager idMan = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      Identity userIdentityObj = idMan.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
      
      LOG.debug("SpaceNotificationPlugin running for " + userId);

      SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
      ListAccess<Space> invitedSpaces = spaceSvc.getInvitedSpacesWithListAccess(userId);
      
      EmailNotificationService notificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);      
      Set<Event> events = notificationService.getEvents(this.getName(), userId);
      for (Space space : invitedSpaces.load(0, invitedSpaces.getSize())) {
        Event event = new Event(space.getPrettyName(), System.currentTimeMillis());
        if (!events.contains(event)) {
          events.add(event);
        }
      }
      
      StringBuilder builder = new StringBuilder();
      String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
      String prefix = "";
      for (Iterator<Event> iter = events.iterator(); iter.hasNext();) {
        Event event = iter.next();
        if (event.getCreatedDate() < lastRun) {
          if (event.getCreatedDate() < DateTimeUtils.subtract7days(lastRun)) {
            iter.remove();
          }
          continue; // bypass if the entry was already reported
        }
        builder.append(prefix);
        prefix = ", ";
        builder.append("<a href='" + host + "/" + messagesCache.getDefault().getProperty("invitedSpaceLink") + "' target='_blank'>" + event.getIdentity() + "</a>");
      }
      
      notificationService.setEvents(this.getName(), userId, events);

      String spaceRequests = builder.toString();
      if(spaceRequests.isEmpty()) return "";

      GroovyTemplate g;
      if (isSummaryMail) {
        g = new GroovyTemplate(messages.getProperty("summary"));
      } else {
        g = new GroovyTemplate(messages.getProperty("message"));
      }
      Map<String, String> binding = new HashMap<String, String>();
      binding.put("spaces", spaceRequests);
      binding.put("tenantName", (String)context.get("repoName"));

      LOG.info("Invitation To Join Space Notification will be sent to " + userIdentityObj.getProfile().getEmail());
      
      return g.render(binding);

    } catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    }
    
    return "";
  }
}
