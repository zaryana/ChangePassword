package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

public class EmailNotificationPluginBean {
	private String name;
	private String setting;
	private boolean isSelected;
	
	public EmailNotificationPluginBean(String name, String setting, boolean isSelected) {
		this.name = name;
		this.setting = setting;
		this.isSelected = isSelected;
	}

	public String getName() {
		return name;
	}

	public String getSetting() {
		return setting;
	}

	public boolean isSelected() {
		return isSelected;
	}
}
