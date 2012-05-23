package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EmailNotificationService {
	private static Log LOG = ExoLogger.getLogger(EmailNotificationService.class);
	
	public static final String HOME = "exo:applications/EmailNotification";
	public static final String PLUGINS = HOME + "/plugins";
	public static final String PREFS = "EmailNotificationPrefs";

	public static final String RESOURCE_DIR = "conf/EmailNotification";
	public static final String PLUGINS_RESOURCE_DIR = RESOURCE_DIR + "/plugins";
	
	private List<EmailNotificationPlugin> plugins;
	
	public EmailNotificationService() {
		plugins = new ArrayList<EmailNotificationPlugin>();
	}
	
	public List<EmailNotificationPlugin> getPlugins() {
		return plugins;
	}

	public void addNotificationPlugin(EmailNotificationPlugin plugin) {
		plugins.add(plugin);
		LOG.info("Plugin added: " + plugin.getName() + " - " + plugin.getDescription());
	}

}






