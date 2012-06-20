/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.platform.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.exoplatform.platform.common.service.MenuConfiguratorService;
import org.exoplatform.platform.webui.NavigationURLUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserNodeFilterConfig;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(lifecycle = UIApplicationLifecycle.class, template = "app:/groovy/platformNavigation/portlet/UIUserPlatformToolBarSitePortlet/UIUserPlatformToolBarSitePortlet.gtmpl")
public class UIUserPlatformToolBarSitePortlet extends UIPortletApplication {
  private static final Log LOG = ExoLogger.getExoLogger(UIUserPlatformToolBarSitePortlet.class);

  private UserACL userACL = null;
  private UserNodeFilterConfig userFilterConfig;
  private MenuConfiguratorService menuConfiguratorService;
  private UserPortalConfigService dataStorage;

  public UIUserPlatformToolBarSitePortlet() throws Exception {
    UserNodeFilterConfig.Builder builder = UserNodeFilterConfig.builder();
    builder.withReadWriteCheck().withVisibility(Visibility.DISPLAYED, Visibility.TEMPORAL).withTemporalCheck();
    userFilterConfig = builder.build();
    userACL = getApplicationComponent(UserACL.class);
    menuConfiguratorService = getApplicationComponent(MenuConfiguratorService.class);
    dataStorage = getApplicationComponent(UserPortalConfigService.class);    
  }

  public boolean hasEditOrCreatePortalPermission() throws Exception {
    List<String> AllowedToEditPortalNames = getAllowedToEditPortalNames();
    return userACL.hasCreatePortalPermission() || AllowedToEditPortalNames.size() > 0;
  }

  private List<String> getAllowedToEditPortalNames() throws Exception {
    List<String> allowedPortalList = new ArrayList<String>();

    List<String> portals = dataStorage.getAllPortalNames();
    for (String portalName : portals) {
      try {
        UserPortalConfig portalConfig = dataStorage.getUserPortalConfig(portalName, getRemoteUser(),
            PortalRequestContext.USER_PORTAL_CONTEXT);
        if (portalConfig != null && userACL.hasEditPermission(portalConfig.getPortalConfig())) {
          allowedPortalList.add(portalName);
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug(getRemoteUser() + " has no permission to access " + portalName);
          }
        }
      } catch (Exception exception) {
        LOG.warn("Can't access to the portal " + portalName, exception);
      }
    }
    return allowedPortalList;
  }

  public List<String> getAllPortalNames() throws Exception {
    List<String> allowedPortalList = new ArrayList<String>();

    List<String> portals = dataStorage.getAllPortalNames();
    for (String portalName : portals) {
      try {
        UserPortalConfig portalConfig = dataStorage.getUserPortalConfig(portalName, getRemoteUser(),
            PortalRequestContext.USER_PORTAL_CONTEXT);
        if (portalConfig != null) {
          allowedPortalList.add(portalName);
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug(getRemoteUser() + " has no permission to access " + portalName);
          }
        }
      } catch (Exception exception) {
        LOG.warn("Can't access to the portal " + portalName);
      }
    }
    return allowedPortalList;
  }
  
  public String getPortalLabel(String portalName) throws Exception
  {
     DataStorage storage_ = getApplicationComponent(DataStorage.class);
     PortalConfig portalConfig = storage_.getPortalConfig(portalName);
     String label = portalConfig.getLabel();
     if (label != null && label.trim().length() > 0)
     {
        return label;
     }

     return portalName;
  }

  public String getCurrentPortal() {
    return Util.getPortalRequestContext().getPortalOwner();
  }

  public UserNavigation getCurrentPortalNavigation() throws Exception {
    return getNavigation(SiteKey.portal(getCurrentPortal()));
  }

  private String getRemoteUser() {
    return Util.getPortalRequestContext().getRemoteUser();
  }

  public UserNode getSelectedPageNode() throws Exception {
    return Util.getUIPortal().getSelectedUserNode();
  }

  private UserPortal getUserPortal() {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    return portalRequestContext.getUserPortal();
  }

  private UserNavigation getNavigation(SiteKey userKey) {
    UserPortal userPortal = getUserPortal();
    return userPortal.getNavigation(userKey);
  }

  public Collection<UserNode> getUserNodes(UserNavigation nav) {
    UserPortal userPortall = getUserPortal();
    if (nav != null) {
      try {
        UserNode rootNode = userPortall.getNode(nav, Scope.ALL, userFilterConfig, null);
        return rootNode.getChildren();
      } catch (Exception exp) {
        LOG.warn(nav.getKey().getName() + " has been deleted");
      }
    }
    return Collections.emptyList();
  }

  public Collection<UserNode> orderUserNodes(Collection<UserNode> userNodes)
  {
    TreeMap<String, UserNode> userNodeTreeMap = new TreeMap<String, UserNode>();
    for (UserNode userNode : userNodes) {
      userNodeTreeMap.put(userNode.getResolvedLabel().toUpperCase() + userNode.getId(), userNode); 
    }
    Collection<UserNode> colection = userNodeTreeMap.values();
    return colection;
  }

  public boolean hasPermissionOnIDENode() throws Exception {
    Identity identity = ConversationState.getCurrent().getIdentity();
    Collection<MembershipEntry> memberships = identity.getMemberships();
    //Get page's access permissions
    Page page = dataStorage.getPage("group::/developers::ide");
    String[] accessPermissions = page.getAccessPermissions();
    //Check if user has access rights to ide page
    for(String permis : accessPermissions) {
      for (MembershipEntry membership: memberships){
        String[] permisSplit = permis.split(":");
        if (permisSplit[1].equals(membership.getGroup())) {
          if (permisSplit[0].contains("*") || membership.getMembershipType().contains("*") || permisSplit[0].equals(membership.getMembershipType())) {
    	    return true;  
    	  }
        }
      }
    }
    return false;
  }

  public String getIDENode() throws Exception {
    List<UserNode> setupMenuUserNodes = menuConfiguratorService.getSetupMenuItems(getUserPortal());
    for (UserNode userNode : setupMenuUserNodes) {
      if ("group::/developers::ide".equals(userNode.getPageRef())) {
        return NavigationURLUtils.getURL(userNode);
      }
	}
    return null;
  }
}
