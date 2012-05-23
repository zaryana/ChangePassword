package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification.plugins.UserJoinTenant;

import java.io.Serializable;

public class UserJoinTenantLogEntry implements Serializable {
	private String userId;
	private long joinDate;

	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
		
	public long getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(long joinDate) {
		this.joinDate = joinDate;
	}
	
	public UserJoinTenantLogEntry(String userId, long joinDate) {
		this.userId = userId;
		this.joinDate = joinDate;
	}
}

