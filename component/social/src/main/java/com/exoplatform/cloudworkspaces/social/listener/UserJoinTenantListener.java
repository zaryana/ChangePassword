package com.exoplatform.cloudworkspaces.social.listener;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserJoinTenantListener extends UserEventListener {

  protected static final Logger LOG = LoggerFactory.getLogger(UserJoinTenantListener.class);
  
  public static final String NEW_USER = "NEW_USER";
  public static final String ACTIVITY_TYPE = "exosocial:newuser";
  
  public void postSave(User user, boolean isNew) throws Exception
  {
    if(isNew)
    {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL userACL = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      String superUserName = userACL.getSuperUser();
      if((null != superUserName) && (user.getUserName().equals(superUserName)))
      {
        return;
      }
      IdentityManager identityManager = Utils.getIdentityManager();
      Identity newIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), false);
      RelationshipManager relationshipManager =  Utils.getRelationshipManager();
      
      //Use SuperUser (root) to connect all users and publish activities 
      Identity superUserIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, superUserName, true);

      Relationship relationship = new Relationship(superUserIdentity, newIdentity, Relationship.Type.CONFIRMED);
      relationshipManager.update(relationship);
      
      //Post activities to SuperUser Identity
      publishActivity(message(newIdentity), newIdentity, superUserIdentity);
      
      
      /*Use virtual indentity to connect all users and post activities
      //Create Application Identity if it not Exit
      Identity appIdentity=getAppIdentity(getApp());
      appIdentity.setDeleted(true);
      identityManager.updateIdentity(appIdentity);
      
      Relationship relationship = new Relationship(appIdentity, newIdentity, Relationship.Type.CONFIRMED);
      relationshipManager.update(relationship);
      
      //Post activities to Application Identity
      publishActivity(message(newIdentity), newIdentity, appIdentity);
      */
      
      LOG.info("\nUserJoinTenantListener: " + user.getUserName() + " has joined tenant");
    }
  }
  
  private Identity getAppIdentity(Application app) throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    ApplicationsIdentityProvider appIdentityProvider = new ApplicationsIdentityProvider();
    appIdentityProvider.addApplication(app);
    identityManager.addIdentityProvider(appIdentityProvider);
    //Identity identity = identityManager.getOrCreateIdentity(ApplicationsIdentityProvider.NAME, app.getId(), true);
    Identity identity = identityManager.getOrCreateIdentity(ApplicationsIdentityProvider.NAME, app.getId(), true);
    return identity;
  }
  
  private Application getApp() {
    Application application = new Application();
    application.setId("Application");
    application.setName("Application Announcer");
    application.setIcon("/social-resources/skin/ShareImages/Avatar.gif");
    application.setUrl("");
    application.setDescription("");
    return application;
  }
  
  private Identity getIdentity(String targetUser) {
    Identity identity = null;
    String[] identityInfo = null;
    try {
     if(targetUser != null && targetUser.split(":").length == 2){
       identityInfo = targetUser.split(":");
     } else {
       throw new Exception();
     }
     ExoContainer container = ExoContainerContext.getCurrentContainer();
     IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
     identity = identityManager.getOrCreateIdentity(identityInfo[0], identityInfo[1], false);
    } catch (Exception e) {
      LOG.warn("Could not find identity for " + targetUser + ": " + e.getMessage());
    }
    return identity;
  }
  
  private String message(Identity newIdentity)throws Exception {
    RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository repository = repoService.getCurrentRepository();
    String tenantName = repository.getConfiguration().getName();
   return "@" + newIdentity.getRemoteId() + " has joined the " + tenantName + " social intranet.";
  }
  
  protected void publishActivity(String message, Identity from, Identity to) throws Exception {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(message);
    activity.setUserId(from.getId());
    activity.setType(ACTIVITY_TYPE);
    
    Map<String,String> params = new HashMap<String,String>();
    params.put(NEW_USER, from.getRemoteId());
    activity.setTemplateParams(params);
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActivityManager activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    activityManager.saveActivityNoReturn(to, activity);

  }
}
