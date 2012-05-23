package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.List;

public class EmailNotificationPrefsBean {
	private String interval;
	private List<EmailNotificationPluginBean> notificationPlugins;

	public String getInterval() {
		return interval;
	}

	public List<EmailNotificationPluginBean> getNotificationPlugins() {
		return notificationPlugins;
	}

	public EmailNotificationPrefsBean(String interval, List<EmailNotificationPluginBean> notificationPlugins) {
		this.interval = interval;
		this.notificationPlugins = notificationPlugins;
	}
}

