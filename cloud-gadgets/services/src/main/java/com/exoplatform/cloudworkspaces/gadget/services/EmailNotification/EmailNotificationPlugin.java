package com.exoplatform.cloudworkspaces.gadget.services.EmailNotification;

import java.util.Map;
import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class EmailNotificationPlugin extends BaseComponentPlugin{
	public abstract String exec(Map<String, Object> context);
}

