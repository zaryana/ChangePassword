package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins;

import java.util.HashMap;
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
import org.exoplatform.social.core.manager.RelationshipManager;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationStorage;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Event;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.Plugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

public class ConnectionNotificationPlugin extends EmailNotificationPlugin{
	private static Log LOG = ExoLogger.getLogger(ConnectionNotificationPlugin.class);
	
	@Override
	public String exec(Map<String, Object> context) {
		try {
			String userId = (String)context.get("userId");
			MessagesCache messagesCache = (MessagesCache)context.get("pluginMessagesCache");
			Properties messages = messagesCache.get((String) context.get("userLocale"));
			long lastRun = (Long)context.get("lastRun");
			
			LOG.debug("ConnectionNotificationPlugin running for " + userId);

			IdentityManager idMan = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
			RelationshipManager relMan = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

			Identity userIdentity = idMan.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
			ListAccess<Identity> rels = relMan.getIncomingWithListAccess(userIdentity);
			
			EmailNotificationStorage notificationStorage = (EmailNotificationStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(EmailNotificationStorage.class);      
      Set<Event> events = notificationStorage.getEvents(Plugin.CONNECTION_REQUEST, userId);
      for (Identity rel : rels.load(0, rels.getSize())) {
        Event event = new Event(rel.getProfile().getFullName(), System.currentTimeMillis());
        if (!events.contains(event)) {
          event.getAttributes().put("url", rel.getProfile().getUrl());
          events.add(event);
        }
      }
			
			StringBuilder builder = new StringBuilder();
			String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
			String prefix = "";
      for (Event event : events) {
        if (event.getCreatedDate() < lastRun)
          continue; // bypass if the entry was already reported
        builder.append(prefix);
        prefix = ", ";
        builder.append("<a href='" + host + "/" + event.getAttributes().get("url") + "' target='_blank'>" + event.getIdentity() + "</a>");
      }
			
			String connectionRequests = builder.toString();
			if(connectionRequests.isEmpty()) return "";

			GroovyTemplate g = new GroovyTemplate(messages.getProperty("message"));
			Map<String, String> binding = new HashMap<String, String>();
			binding.put("connections", connectionRequests);
			
			return g.render(binding);

		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		}

		return "";
	}
}
