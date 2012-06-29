package com.exoplatform.cloudworkspaces.portlet.FeedbackPortlet;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/groovy/FeedbackPortlet/UIFeedbackMainContainer.gtmpl")
public class UIFeedbackMainContainer extends UIContainer {
  public UIFeedbackMainContainer() throws Exception {
    super();
    if (this.getChild(UIFeedbackForm.class) == null) {
      this.addChild(UIFeedbackForm.class, null, null);
    }
  }
}
