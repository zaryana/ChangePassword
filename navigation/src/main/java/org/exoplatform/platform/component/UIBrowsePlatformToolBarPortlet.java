/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.component;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import com.exoplatform.cloudworkspaces.social.space.statistic.VisitedSpaceService;

/**
 * Portlet manages profile.<br>
 */
@ComponentConfig(lifecycle = UIApplicationLifecycle.class,
                 template = "app:/groovy/platformNavigation/portlet/UIBrowsePlatformToolBarPortlet/UIBrowsePlatformToolBarPortlet.gtmpl", 
                 events = { @EventConfig(listeners = UIBrowsePlatformToolBarPortlet.NavigationChangeActionListener.class) })
                 
public class UIBrowsePlatformToolBarPortlet extends UIPortletApplication {

  private String currentPortalName = null;
  private boolean socialPortal = false;

  public UIBrowsePlatformToolBarPortlet() throws Exception {
    super();
  }
  
  private String getCurrentPortalName() {
    return Util.getPortalRequestContext().getPortalOwner();
  }
  
  public boolean isSocialProfileActivated() {
    return (ExoContainer.getProfiles().contains("social") || ExoContainer.getProfiles().contains("default") || ExoContainer
        .getProfiles().contains("all"));
  }
  
  public static UserPortal getUserPortal() {
    UserPortalConfig portalConfig = Util.getPortalRequestContext().getUserPortalConfig();
    return portalConfig.getUserPortal();
  }
  
  public boolean isSocialPortal() {
    if (currentPortalName != null && getCurrentPortalName().equals(currentPortalName)) {
      return socialPortal;
    }
    if (!isSocialProfileActivated()) {
      socialPortal = false;
    } else {
      currentPortalName = getCurrentPortalName();
      UserPortal userPortal = getUserPortal();
      UserNavigation userNavigation = userPortal.getNavigation(SiteKey.portal(currentPortalName));
      UserNode portalNode = userPortal.getNode(userNavigation, Scope.CHILDREN, null, null);
      socialPortal = portalNode.getChild("spaces") != null;
    }
    return socialPortal;
  }
  
  public List<String> getLastVisitedSpaces() {
    String userId = Util.getPortalRequestContext().getRemoteUser();
    VisitedSpaceService visitedSpaceService = getApplicationComponent(VisitedSpaceService.class);
    List<String> lastVisitedSpace = visitedSpaceService.getVisitedSpacesList(userId);
    return lastVisitedSpace;
  }
  
  public String getSpaceDisplayName(String spacePrettyName) {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    if (space != null) return space.getDisplayName();
    return null;
  }
  
  public static class NavigationChangeActionListener extends EventListener<UIBrowsePlatformToolBarPortlet> {
    @Override
    public void execute(Event<UIBrowsePlatformToolBarPortlet> event) throws Exception {
    }
  }
    
}
