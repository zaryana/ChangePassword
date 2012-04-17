/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package com.exoplatform.cloudworkspaces.gadget.services.GettingStarted;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("getting-started/")
@Produces(MediaType.APPLICATION_JSON)
public class GettingStartedRestService implements ResourceContainer {
  private static final Log          log = ExoLogger.getLogger(GettingStartedRestService.class);

  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  /**
   * Get user's getting started status REST service URL: /getting-started/get
   * 
   * @return: user's getting started status
   */
  @GET
  @Path("get")
  public Response get(@Context
  SecurityContext sc, @Context
  UriInfo uriInfo) throws Exception {
    try {
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      if (userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
      }

      NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer()
                                                                                   .getComponentInstanceOfType(NodeHierarchyCreator.class);
      SessionProvider sProvider = SessionProvider.createSystemProvider();
      Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("ApplicationData");
      if (!userPrivateNode.hasNode("GsGadget")) {
        Node gettingStartedNode = userPrivateNode.addNode("GsGadget");
        userPrivateNode.save();
        gettingStartedNode.setProperty("exo:gs_profile", false);
        gettingStartedNode.setProperty("exo:gs_invite", false);
        gettingStartedNode.setProperty("exo:gs_connect", false);
        gettingStartedNode.setProperty("exo:gs_space", false);
        gettingStartedNode.setProperty("exo:gs_mobile", false);
        gettingStartedNode.setProperty("exo:gs_wiki", false);
        gettingStartedNode.setProperty("exo:gs_dashboard", false);
        gettingStartedNode.save();
      }

      Node gettingStartedNode = userPrivateNode.getNode("GsGadget");
      gettingStartedNode.setProperty("exo:gs_profile", hasAvatar(userId));
      gettingStartedNode.setProperty("exo:gs_connect", hasContacts(userId));
      gettingStartedNode.setProperty("exo:gs_space", hasSpaces(userId));
      gettingStartedNode.setProperty("exo:gs_dashboard", hasGadgets(userId));

      PropertyIterator propertiesIt = userPrivateNode.getNode("GsGadget").getProperties("exo:gs_*");
      JSONArray jsonArray = new JSONArray();

      while (propertiesIt.hasNext()) {
        Property prop = (Property) propertiesIt.next();
        JSONObject json = new JSONObject();
        json.put("name", prop.getName());
        json.put("value", prop.getString());
        jsonArray.put(json);
      }
      return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON)
                     .cacheControl(cacheControl)
                     .build();
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
  }

  @GET
  @Path("set/{property}/{value}")
  public Response set(@PathParam("property")
  String property, @PathParam("value")
  Boolean value, @Context
  SecurityContext sc, @Context
  UriInfo uriInfo) throws Exception {
    try {
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      if (userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
      }
      setGsProperty(property, value, sc, userId);
      return Response.ok("{\"status\":\"successed\"}", MediaType.APPLICATION_JSON)
                     .cacheControl(cacheControl)
                     .build();
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
  }

  private void setGsProperty(String property, Boolean value, SecurityContext sc, String userId) {

    try {
      NodeHierarchyCreator nodeCreator = (NodeHierarchyCreator) ExoContainerContext.getCurrentContainer()
                                                                                   .getComponentInstanceOfType(NodeHierarchyCreator.class);
      SessionProvider sProvider = SessionProvider.createSystemProvider();
      Node userPrivateNode = nodeCreator.getUserNode(sProvider, userId).getNode("ApplicationData");
      if (!userPrivateNode.hasNode("GsGadget")) {
        userPrivateNode.addNode("GsGadget");
        userPrivateNode.save();
      }
      Node gettingStartedNode = userPrivateNode.getNode("GsGadget");
      if (gettingStartedNode.hasProperty(property)) {
        gettingStartedNode.setProperty(property, value);
        gettingStartedNode.save();
      }

    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("deprecation")
  private boolean hasAvatar(String userId) {
    try {
      IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                              userId);
      Profile profile = identity.getProfile();

      if (profile.getAvatarUrl() != null)
        return true;
      else
        return false;
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  private boolean hasSpaces(String userId) {
    try {
      SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer()
                                                                    .getComponentInstanceOfType(SpaceService.class);
      List<Space> spaces = spaceService.getAccessibleSpaces(userId);

      if (spaces.size() != 0)
        return true;
      else
        return false;
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  private boolean hasContacts(String userId) {
    try {

      IdentityManager identityManager = (IdentityManager) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(IdentityManager.class);
      RelationshipManager relationshipManager = (RelationshipManager) ExoContainerContext.getCurrentContainer()
                                                                                         .getComponentInstanceOfType(RelationshipManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                              userId);
      List<Relationship> confirmedContacts = relationshipManager.getContacts(identity);

      if (confirmedContacts.size() != 0)
        return true;
      else
        return false;
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }

  @SuppressWarnings({ "deprecation", "rawtypes" })
  private boolean hasGadgets(String userId) {
    try {
      UserPortalConfigService userPortalConfigService = (UserPortalConfigService) ExoContainerContext.getCurrentContainer()
                                                                                                     .getComponentInstanceOfType(UserPortalConfigService.class);
      UserPortalConfig portalConfig = userPortalConfigService.getUserPortalConfig(userPortalConfigService.getDefaultPortal(),
                                                                                  userId);
      UserPortal userPortal = portalConfig.getUserPortal();
      UserNavigation userNavigation = userPortal.getNavigation(SiteKey.user(userId));
      UserNode rootNode = userPortal.getNode(userNavigation, Scope.ALL, null, null);
      Collection<UserNode> nodes = rootNode.getChildren();

      // for all dashboard pages
      for (UserNode node : nodes) {
        DataStorage dataStorageService = (DataStorage) ExoContainerContext.getCurrentContainer()
                                                                          .getComponentInstanceOfType(DataStorage.class);
        List<ModelObject> children = dataStorageService.getPage(node.getPageRef()).getChildren();

        // for each portlet container
        for (Object child : children) {
          if (child instanceof Application) {
            Application application = (Application) child;
            if (application.getType() == ApplicationType.PORTLET) {
              Dashboard dashboard = dataStorageService.loadDashboard(application.getStorageId());
              List<ModelObject> dashboardChildren = dashboard.getChildren();
              // for each dashboard column
              for (ModelObject dashboardChild : dashboardChildren) {
                if (dashboardChild instanceof Container) {
                  List<ModelObject> columnChildren = ((Container) dashboardChild).getChildren();
                  if (columnChildren.size() != 0)
                    return true;
                }
              }
            }
          }
        }
      }
      return false;
    } catch (Exception e) {
      log.debug("Error in gettingStarted REST service: " + e.getMessage(), e);
      return false;
    }
  }
}
