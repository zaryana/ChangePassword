package com.exoplatform.cloudworkspaces.portlet.FeedbackPortlet;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/FeedbackPortlet/UIFeedbackPortlet.gtmpl")
public class UIFeedbackPortlet extends UIPortletApplication {

  public UIFeedbackPortlet() throws Exception {
    super();
    if (this.getChild(UIFeedbackMainContainer.class) == null) {
      this.addChild(UIFeedbackMainContainer.class, null, null);
    }
  }
}
