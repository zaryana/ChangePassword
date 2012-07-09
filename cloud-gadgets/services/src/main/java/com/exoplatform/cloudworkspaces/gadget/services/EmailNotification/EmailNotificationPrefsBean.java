package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.List;

public class EmailNotificationPrefsBean {
  private boolean isSummaryMail;
	private String interval;
	private List<EmailNotificationPluginBean> notificationPlugins;

  public boolean isSummaryMail() {
    return isSummaryMail;
  }
	
	public String getInterval() {
		return interval;
	}

	public List<EmailNotificationPluginBean> getNotificationPlugins() {
		return notificationPlugins;
	}

	public EmailNotificationPrefsBean(Boolean isSummaryMail, String interval, List<EmailNotificationPluginBean> notificationPlugins) {
	  this.isSummaryMail = isSummaryMail;
		this.interval = interval;
		this.notificationPlugins = notificationPlugins;
	}
}

