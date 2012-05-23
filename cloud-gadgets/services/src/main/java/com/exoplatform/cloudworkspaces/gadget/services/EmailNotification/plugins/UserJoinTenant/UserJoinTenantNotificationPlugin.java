package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

public class UserJoinTenantNotificationPlugin extends EmailNotificationPlugin{
	private static Log LOG = ExoLogger.getLogger(UserJoinTenantNotificationPlugin.class);

	@Override
	public String exec(Map<String, Object> context) {
		try {
			String userId = (String)context.get("userId");
			MessagesCache messagesCache = (MessagesCache)context.get("pluginMessagesCache");
			Properties messages = messagesCache.get((String) context.get("userLocale"));
			long lastRun = (Long)context.get("lastRun");
			
			LOG.debug("UserJoinTenantNotificationPlugin running for " + userId);

			IdentityManager idMan = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
			UserJoinTenantLog joinLog = (UserJoinTenantLog) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserJoinTenantLog.class);
			List<UserJoinTenantLogEntry> logEntries = joinLog.getLog();

			StringBuilder builder = new StringBuilder();
			String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
			String prefix = "";
			for(UserJoinTenantLogEntry logEntry:logEntries) {
				if(logEntry.getJoinDate() < lastRun) continue; // bypass if the entry was already reported
				builder.append(prefix);
				prefix = ", ";
				Identity userIdentity = idMan.getOrCreateIdentity(OrganizationIdentityProvider.NAME, logEntry.getUserId(), false);
				Profile userProfile = userIdentity.getProfile();
				builder.append(builder.append("<a href='" + host + "/" + userProfile.getUrl() + "' target='_blank'>" + userProfile.getFullName() + "</a>"));
			}
			
			String usersJoined = builder.toString();
			if(usersJoined.isEmpty()) return "";

			GroovyTemplate g = new GroovyTemplate(messages.getProperty("message"));
			Map<String, String> binding = new HashMap<String, String>();
			binding.put("users", usersJoined);
			binding.put("tenantName", (String)context.get("repoName"));
			
			return g.render(binding);
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		}
		
		return "";
	}
}
