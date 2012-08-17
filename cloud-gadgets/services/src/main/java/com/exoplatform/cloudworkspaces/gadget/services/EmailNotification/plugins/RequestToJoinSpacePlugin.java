/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.DateTimeUtils;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationService;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Plugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          vietnt@exoplatform.com
 * Jul 19, 2012  
 */
public class RequestToJoinSpacePlugin extends EmailNotificationPlugin {

  private static Log LOG = ExoLogger.getLogger(RequestToJoinSpacePlugin.class);

  @Override
  public String exec(Map<String, Object> context) {
    try {
      String userId = (String) context.get("userId");
      MessagesCache messagesCache = (MessagesCache) context.get("pluginMessagesCache");
      Properties messages = messagesCache.get((String) context.get("userLocale"));
      long lastRun = (Long) context.get("lastRun");
      boolean isSummaryMail = (Boolean) context.get("isSummaryMail");

      SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
      ListAccess<Space> settingableSpaces = spaceSvc.getSettingableSpaces(userId);

      EmailNotificationService notificationService = (EmailNotificationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationService.class);
      Set<Event> events = notificationService.getEvents(Plugin.REQUEST_JOIN_SPACE, userId);
      for (Space space : settingableSpaces.load(0, settingableSpaces.getSize())) {
        if (space.getPendingUsers() != null) {
          for (String user : space.getPendingUsers()) {
            Event event = new Event(user + "_" + space.getPrettyName(), System.currentTimeMillis());
            if (!events.contains(event)) {
              event.getAttributes().put("user", user);
              event.getAttributes().put("space", space.getPrettyName());
              events.add(event);
            }
          }
        }
      }
      
      IdentityManager idMan = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      StringBuilder builder = new StringBuilder();
      StringBuilder builder2 = new StringBuilder();
      String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
      String prefix = "";
      String prefix2 = "";
      for (Iterator<Event> iter = events.iterator(); iter.hasNext();) {
        Event event = iter.next();
        if (event.getCreatedDate() < lastRun) {
          if (event.getCreatedDate() < DateTimeUtils.subtract7days(lastRun)) {
            iter.remove();
          }
          continue; // bypass if the entry was already reported
        }
        builder.append(prefix);
        builder2.append(prefix2);
        prefix = ", ";
        prefix2 = ", ";
        builder.append("<a href='" + host + "/" + messagesCache.getDefault().getProperty("spacesLink") + "' target='_blank'>" + event.getAttributes().get("space") + "</a>");
        Identity userIdentity = idMan.getOrCreateIdentity(OrganizationIdentityProvider.NAME, event.getAttributes().get("user"), false);
        Profile userProfile = userIdentity.getProfile();
        builder2.append("<a href='" + host + "/" + userProfile.getUrl() + "' target='_blank'>" + userProfile.getFullName() + "</a>");
      }
      
      notificationService.setEvents(Plugin.REQUEST_JOIN_SPACE, userId, events);
      
      String spaceRequests = builder.toString();
      String userRequests = builder2.toString();
      if(spaceRequests.isEmpty()) return "";

      GroovyTemplate g;
      if (isSummaryMail) {
        g = new GroovyTemplate(messages.getProperty("summary"));
      } else {
        g = new GroovyTemplate(messages.getProperty("message"));
      }
      Map<String, String> binding = new HashMap<String, String>();
      binding.put("spaces", spaceRequests);
      binding.put("users", userRequests);
      binding.put("tenantName", (String)context.get("repoName"));

      return g.render(binding);
      
    } catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    }
    return "";
  }

}
