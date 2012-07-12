package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.EmailNotificationPlugin;
import com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.utils.MessagesCache;

public class SpaceNotificationPlugin extends EmailNotificationPlugin{
	private static Log LOG = ExoLogger.getLogger(SpaceNotificationPlugin.class);
	
	@Override
	public String exec(Map<String, Object> context) {
		try {
			String userId = (String)context.get("userId");
			MessagesCache messagesCache = (MessagesCache)context.get("pluginMessagesCache");
			Properties messages = messagesCache.get((String) context.get("userLocale"));

			LOG.debug("SpaceNotificationPlugin running for " + userId);

			SpaceService spaceSvc = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
			ListAccess<Space> invitedSpaces = spaceSvc.getInvitedSpacesWithListAccess(userId);
			
			StringBuilder builder = new StringBuilder();
			String host = context.get("repoName") + "." + System.getProperty("tenant.masterhost");
			String prefix = "";
			for(Space space:invitedSpaces.load(0, invitedSpaces.getSize())) {
				builder.append(prefix);
				prefix = ", ";
				builder.append("<a href='" + host + "/" + messagesCache.getDefault().getProperty("invitedSpaceLink") + "' target='_blank'>" + space.getPrettyName() + "</a>");
			}

			String spaceRequests = builder.toString();
			if(spaceRequests.isEmpty()) return "";

			GroovyTemplate g = new GroovyTemplate(messages.getProperty("message"));
			Map<String, String> binding = new HashMap<String, String>();
			binding.put("spaces", spaceRequests);

			return g.render(binding);

		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
		}
		
		return "";
	}
}
