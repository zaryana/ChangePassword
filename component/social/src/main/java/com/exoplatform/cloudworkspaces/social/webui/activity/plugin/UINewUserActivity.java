/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package com.exoplatform.cloudworkspaces.social.webui.activity.plugin;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.social.core.relationship.model.Relationship;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/component/webui/activity/plugin/UINewUserActivity.gtmpl",
  events = {
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class,
                 confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class,
                 confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"),
    @EventConfig(listeners = UINewUserActivity.IgnoreActionListener.class), 
    @EventConfig(listeners = UINewUserActivity.RequestToConnectActionListener.class),
    @EventConfig(listeners = UINewUserActivity.ConfirmActionListener.class)
  }
)
public class UINewUserActivity extends BaseUIActivity {

  private static final String INVITATION_ESTABLISHED_INFO = "NewUserActitity.label.InvitationEstablishedInfo";
  private static final String INVITATION_REVOKED_INFO = "NewUserActitity.label.RevokedInfo";
  
  public static final String ACTIVITY_TYPE = "exosocial:newuser";
  public static final String NEW_USER = "NEW_USER";
  
  private String newUserName;
  private Identity newUser;

  public String getNewUserName() {
    return newUserName;
  }

  public void setNewUserName(String newUserName) {
    this.newUserName = newUserName;
  }

  public Identity getNewUser() {
    if (newUser == null) {
      newUser = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, newUserName, true);
    }
    return newUser;
  }

  public void setNewUser(Identity newUser) {
    this.newUser = newUser;
  }
  
  public String getNewUserFirstName() {
    String newUserFirstName = (String)(getNewUser().getProfile().getProperty(org.exoplatform.social.core.identity.model.Profile.FIRST_NAME));
    return newUserFirstName;
  }
  public boolean isActivityStreamOwner() {
    UIActivitiesContainer uiActivititesContainer = getAncestorOfType(UIActivitiesContainer.class);
    return Utils.getViewerRemoteId().equals(uiActivititesContainer.getOwnerName());
  }

  public boolean isNewUser() throws Exception {
    return Utils.getViewerRemoteId().equals(newUserName);
  }
  
  
  public Relationship getRelationship() throws Exception {
    Identity newUser = getNewUser();
    if (newUser.equals(Utils.getViewerIdentity())) {
      return null;
    }
    Relationship relationship = Utils.getRelationshipManager().get( Utils.getViewerIdentity(),newUser);
    return relationship;
  }
  
  public String getActivityTitle(WebuiBindingContext ctx) throws Exception {
    UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
    if (uiUserActivitiesDisplay == null) {
      return null;
    }
    
    RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository = repoService.getCurrentRepository();
    String tenantName = repository.getConfiguration().getName();
    
    if(isNewUser())
    {
      return ctx.appRes("NewUserActitity.msg.title-himself").replace("{0}", tenantName);
    }
    else{
      String newUserLink = LinkProvider.getProfileLink(newUserName);
      return ctx.appRes("NewUserActitity.msg.title").replace("{0}", newUserLink).replace("{1}", tenantName);
    }
  }
  
  public static class IgnoreActionListener extends EventListener<UINewUserActivity> {
    @Override
    public void execute(Event<UINewUserActivity> event) throws Exception {

      UINewUserActivity uiNewUserActivity = (UINewUserActivity) event.getSource();

      Identity invitedIdentity = uiNewUserActivity.getNewUser();
      Identity invitingIdentity = Utils.getViewerIdentity();

      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, invitedIdentity);
      
      if (relationship != null && relationship.getStatus() == Relationship.Type.CONFIRMED) {
        Utils.getRelationshipManager().delete(relationship);
        return;
      }
      
      if (relationship == null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.getRelationshipManager().deny(invitedIdentity, invitingIdentity);
      Utils.updateWorkingWorkSpace();
    }
  }
  
  public static class RequestToConnectActionListener extends EventListener<UINewUserActivity> {
    @Override
    public void execute(Event<UINewUserActivity> event) throws Exception {
      
      UINewUserActivity uiNewUserActivity = (UINewUserActivity) event.getSource();

      Identity invitedIdentity = uiNewUserActivity.getNewUser();
      Identity invitingIdentity = Utils.getViewerIdentity();
      
      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, invitedIdentity);
      
      if (relationship != null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_ESTABLISHED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.getRelationshipManager().inviteToConnect(invitingIdentity, invitedIdentity);
      Utils.updateWorkingWorkSpace();
    }
  }
  
  public static class ConfirmActionListener extends EventListener<UINewUserActivity> {
    public void execute(Event<UINewUserActivity> event) throws Exception {
      UINewUserActivity uiNewUserActivity = (UINewUserActivity) event.getSource();

      Identity invitedIdentity = uiNewUserActivity.getNewUser();
      Identity invitingIdentity = Utils.getViewerIdentity();

      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, invitedIdentity);

      if (relationship == null || relationship.getStatus() != Relationship.Type.PENDING) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.getRelationshipManager().confirm(invitedIdentity, invitingIdentity);
      Utils.updateWorkingWorkSpace();
    }
  }
   
}