package com.exoplatform.cloudworkspaces.portlet.EmailNotificationSettingPortlet;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.UIPortletApplication;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/EmailNotificationSettingPortlet/EmailNotificationSettingPortlet.gtmpl"
)
public class EmailNotificationSettingPortlet extends UIPortletApplication {

  public EmailNotificationSettingPortlet() throws Exception {   
  }
}
