package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

public class ConnectionNotificationPlugin extends EmailNotificationPlugin{
	private static Log LOG = ExoLogger.getLogger(ConnectionNotificationPlugin.class);
	
	@Override
	public String exec(Map<String, Object> context) {
		try {
			String userId = (String)context.get("userId");
			MessagesCache messagesCache = (MessagesCache)context.get("pluginMessagesCache");
			Properties messages = messagesCache.get((String) context.get("userLocale"));
			
			LOG.debug("ConnectionNotificationPlugin running for " + userId);

			IdentityManager idMan = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
			RelationshipManager relMan = (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);

			Identity userIdentity = idMan.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
			ListAccess<Identity> rels = relMan.getIncomingWithListAccess(userIdentity);
			
			StringBuilder builder = new StringBuilder();
			String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
			String prefix = "";
			for(Identity rel:rels.load(0, rels.getSize())) {
				builder.append(prefix);
				prefix = ", ";
				builder.append("<a href='" + host + "/" + rel.getProfile().getUrl() + "' target='_blank'>" + rel.getProfile().getFullName() + "</a>");
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
