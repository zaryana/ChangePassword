/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.exoplatform.cloudworkspaces.cloudlogin.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.ws.rs.core.CacheControl;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.upload.UploadResource;

import com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService;
import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * Service used to manage all business with CloudLoginWizard
 * 
 * @author Clement
 *
 */
public class CloudLoginServiceImpl implements CloudLoginService {

  private static Log logger = ExoLogger.getLogger(CloudLoginServiceImpl.class);
  private RepositoryService repositoryService;
  private OrganizationService organizationService;

  private static final String LOGIN_HISTORY_HOME = "exo:LoginHistoryHome";
  private static final String CL_MIXIN_TYPE = "exo:cloudlogin";
  private static final String CL_MIXIN_STATUS = "exo:cloudLoginStatus";
  private static final Pattern emailPattern = Pattern.compile("^([a-zA-Z0-9_\\.\\-])+\\@((([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+)$", Pattern.CASE_INSENSITIVE);

  // All nodes name used to have a temporary avatar into JCR
  private static final String CL_JCR_ROOT_NODE_PATH = "/rest/jcr/repository/collaboration";
  private static final String CL_JCR_APP_NODE_NAME = "Documents";
  private static final String CL_JCR_FOLDER_NAME = "cloudlogin";

  
  /*=======================================================================
   * Component access
   *======================================================================*/
  
  /**
   * Utility function to get JCR session in current repository
   * 
   * @param sessionProvider
   * @return JCR session
   * @throws Exception
   */     
  private Session getSession(SessionProvider sessionProvider) throws Exception {
    ManageableRepository currentRepo = this.repositoryService.getCurrentRepository();
    return sessionProvider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  public CloudLoginServiceImpl(RepositoryService repositoryService, OrganizationService organizationService) {
    this.repositoryService = repositoryService;
    this.organizationService = organizationService;
  }

  
  /*=======================================================================
   * Public API methods
   *======================================================================*/
  
  @Override
  public CloudLoginStatus getStatus(String userId) {
    CloudLoginStatus cloudLoginStatus = CloudLoginStatus.NONE;
    
    Node cloudUserNode = getCloudUserNode(userId);
    if(cloudUserNode != null) {
      try {
        if(cloudUserNode.hasProperty(CL_MIXIN_STATUS)) {
          Property pp = cloudUserNode.getProperty(CL_MIXIN_STATUS);
          if(pp != null) {
            cloudLoginStatus = CloudLoginStatus.getCloudLoginStatus(pp.getString());
          }
        }
      }
      catch (ConstraintViolationException e) {
        // case of property CL_MIXIN_STATUS doesn't exist
        logger.warn("Cloud Login: JCR property '" + CL_MIXIN_STATUS + "' doesn't exist");
      }
      catch (RepositoryException e) {
        logger.error("Cloud Login: cannot get status for user '" + userId + "'", e);
      }
    }
    
    return cloudLoginStatus;
  }

  @Override
  public CloudLoginStatus setStatus(String userId, CloudLoginStatus status) {
    
    Node cloudUserNode = null;

    if(! hasCloudLoginMixin(userId)) {
      cloudUserNode = createCloudLoginStatus(userId);
    }
    else {
      if(logger.isDebugEnabled()) {
        logger.debug("Cloud Login: status yet created for user '" + userId + "'");
      }
      cloudUserNode = getCloudUserNode(userId);
    }
    
    if(cloudUserNode != null) {
      try {
        cloudUserNode.setProperty(CL_MIXIN_STATUS, status);
        cloudUserNode.save();
      }
      catch (ConstraintViolationException e) {
        // case of property CL_MIXIN_STATUS doesn't exist
        logger.warn("Cloud Login: JCR property '" + CL_MIXIN_STATUS + "' doesn't exist");
      }
      catch (RepositoryException e) {
        logger.error("Cloud Login: impossible to set status to '" + status + "' with user '" + userId + "'", e);
      }
    }
    else {
      logger.error("Cloud Login: impossible to set status to '" + status + "' with user '" + userId + "'");
    }
    
    return null;
  }
  
  @Override
  public String getCloudTenantDomain() {
    
    String domain = "";

    RequestLifeCycle.begin(PortalContainer.getInstance());
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    
    try {
      ListAccess<User> list = organizationService.getUserHandler().findUsersByGroupId("/platform/administrators");
      
      if(list != null && list.getSize() > 0) {
        User[] users = list.load(0, 1);
        if(users != null && users.length > 0) {
          User tenantOwner = users[0];
          if(tenantOwner != null) {
            if(logger.isDebugEnabled()) {
              logger.debug("We found the tenant owner: " + tenantOwner + " with mail: " + tenantOwner.getEmail());
            }
            domain = extractDomainFromEmail(tenantOwner.getEmail());
          }
          else {
            logger.info("There is no tenant owner into group /platform/administrators");
          }
        }
        else {
          logger.info("There is no users into group /platform/administrators");
        }
      }
      else {
        logger.info("List of users into group /platform/administrators is empty");
      }
    }
    catch(Exception e) {
      logger.error("CloudLogin: Cannot get domain from tenant", e);
    }
    finally {
      try {
        RequestLifeCycle.end();
      } catch (Exception e) {
        logger.warn("An exception has occurred while proceed RequestLifeCycle.end() : " + e.getMessage());
      }
    }
    
    return domain;
  }
  
  @Override
  public String createTempAvatarNode(UploadResource upResource) {
    
    String avatarUri = "";

    RequestLifeCycle.begin(PortalContainer.getInstance());
    
    try {
      String fileName = upResource.getFileName();
      Node avatarRootNode = getAvatarRootNode();
      
      if(! avatarRootNode.hasNode(fileName)) {
        String location = upResource.getStoreLocation();
        String mimeType = upResource.getMimeType();
        
        //resize image with avatar attachment
        File file = new File(location);
        FileInputStream inputStream = new FileInputStream(file);
        AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream, 200, 0, null, fileName, mimeType, null);
        if(avatarAttachment == null) {
          avatarAttachment = new AvatarAttachment(null, fileName, upResource.getMimeType(), inputStream, null, System.currentTimeMillis());
        }
        byte[] uploadData = avatarAttachment.getImageBytes();
        
        // Add the node
        Node nodeFile = avatarRootNode.addNode(fileName,FCKUtils.NT_FILE);
        Node jcrContent = nodeFile.addNode("jcr:content","nt:resource");
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        String mimetype = mimeTypeResolver.getMimeType(upResource.getFileName());
        // Add the data to the node
        jcrContent.setProperty("jcr:data",new ByteArrayInputStream(uploadData));
        jcrContent.setProperty("jcr:lastModified",new GregorianCalendar());
        jcrContent.setProperty("jcr:mimeType",mimetype);
        // Save session of node
        avatarRootNode.getSession().save();
        avatarRootNode.getSession().refresh(true); // Make refreshing data
        
        avatarUri = CL_JCR_ROOT_NODE_PATH + nodeFile.getPath();
      }
      else {
        Node avatarNode = avatarRootNode.getNode(fileName);
        avatarUri = CL_JCR_ROOT_NODE_PATH + avatarNode.getPath();
      }
    }
    catch(Exception e) {
      logger.error("CloudLogin: Cannot create temp avatar node", e);
    }
    finally {
      try {
        RequestLifeCycle.end();
      } catch (Exception e) {
        logger.warn("An exception has occurred while proceed RequestLifeCycle.end() : " + e.getMessage());
      }
    }
    
    return avatarUri;
  }
  
  @Override
  public void deleteTempAvatarNode() {

    RequestLifeCycle.begin(PortalContainer.getInstance());
    
    try {

      // Get node
      Node avatarRootNode = getAvatarRootNode();
      if(avatarRootNode != null) {
        avatarRootNode.remove();
        
        // Save session of node
        avatarRootNode.getSession().save();
        avatarRootNode.getSession().refresh(true); // Make refreshing data
      }
      else {
        logger.warn("CloudLogin: Cannot delete Node " + CL_JCR_FOLDER_NAME + ", it should exist");
      }
    }
    catch(Exception e) {
      logger.error("CloudLogin: Cannot delete temp avatar node", e);
    }
    finally {
      try {
        RequestLifeCycle.end();
      } catch (Exception e) {
        logger.warn("An exception has occurred while proceed RequestLifeCycle.end() : " + e.getMessage());
      }
    }
  }

  @Override
  public void updateProfile(String userId, UploadResource avatarResource, String firstName, String lastName, String position) {

    RequestLifeCycle.begin(PortalContainer.getInstance());
    
    try {

      Profile p = Utils.getUserIdentity(userId, true).getProfile();
      boolean profileNeedUpdate = false;
      
      // Update avatar
      if(avatarResource != null) {
        profileNeedUpdate = true;
        File file = new File(avatarResource.getStoreLocation());
        FileInputStream inputStream = new FileInputStream(file);
        String mimeType = avatarResource.getMimeType();
        String fileName = file.getName();
        
        // Create avatar attachement
        AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream, 200, 0, null, fileName, mimeType, null);
        if(avatarAttachment == null) {
          avatarAttachment = new AvatarAttachment(null, fileName, avatarResource.getMimeType(), inputStream, null, System.currentTimeMillis());
        }
        
        // Update profile with new avatar
        p.setProperty(Profile.AVATAR, avatarAttachment);
        
        Map<String, Object> props = p.getProperties();
        // Removes avatar url and resized avatar
        for (String key : props.keySet()) {
          if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
            p.removeProperty(key);
          }
        }
      }
      if(firstName != null && firstName.length() > 0) {
        profileNeedUpdate = true;
        p.setProperty(Profile.FIRST_NAME, firstName);
      }
      if(lastName != null && lastName.length() > 0) {
        profileNeedUpdate = true;
        p.setProperty(Profile.LAST_NAME, lastName);
      }
      if(position != null && position.length() > 0) {
        profileNeedUpdate = true;
        p.setProperty(Profile.POSITION, position);
      }
      
      if(profileNeedUpdate) {
        Utils.getIdentityManager().updateProfile(p);
      }
    }
    catch(Exception e) {
      logger.error("CloudLogin: Cannot update profile", e);
    }
    finally {
      try {
        RequestLifeCycle.end();
      } catch (Exception e) {
        logger.warn("An exception has occurred while proceed RequestLifeCycle.end() : " + e.getMessage());
      }
    }
  }
  
  
  
  /*=======================================================================
   * API private methods
   *======================================================================*/
  
  /**
   * Create cloud login node with status NONE
   * <p>
   * If there is a problem, returns null
   * @param userId
   * @returns Node
   */
  private Node createCloudLoginStatus(String userId) {
    
    Node cloudUserNode = null;

    if(userId != null) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try{
        Session session = this.getSession(sessionProvider);
        Node rootNode = session.getRootNode();
        if(rootNode != null && rootNode.hasNode(LOGIN_HISTORY_HOME)) {
          Node homeNode = rootNode.getNode(LOGIN_HISTORY_HOME);
          if(homeNode != null && homeNode.hasNode(userId)){
            cloudUserNode = homeNode.getNode(userId);
            if(cloudUserNode != null) {
              if(cloudUserNode.canAddMixin(CL_MIXIN_TYPE)) {
                cloudUserNode.addMixin(CL_MIXIN_TYPE);
                cloudUserNode.setProperty(CL_MIXIN_STATUS, CloudLoginStatus.NONE);
                cloudUserNode.save();
              }
              else {
                logger.error("Cloud Login: Error while adding cloud login status for user '" + userId + "'");
              }
            }
          }
          else {
            logger.error("Cloud Login: Node '" + rootNode.getPath() + LOGIN_HISTORY_HOME + "/" + userId + "' should exist.");
          }
        }
        else {
          logger.error("Cloud Login: Node '" + rootNode.getPath() + LOGIN_HISTORY_HOME + "' should exist.");
        }
      }
      catch (NoSuchNodeTypeException e) {
        // case of CL_MIXIN_TYPE doesn't exist
        logger.warn("Cloud Login: Problem with mixin '" + CL_MIXIN_TYPE + "'");
      }
      catch (ConstraintViolationException e) {
        // case of property CL_MIXIN_STATUS doesn't exist
        logger.warn("Cloud Login: JCR property '" + CL_MIXIN_STATUS + "' doesn't exist");
      }
      catch (Exception e) {
        logger.error("Cloud Login: Error while adding cloud login status for user '" + userId + "'", e);
      }
    }
    
    return cloudUserNode;
  }

  /**
   * 
   * @param userName
   * @return cloud login node
   */
  private Node getCloudUserNode(String userId) {

    Node cloudUserNode = null;

    if(userId != null) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try{
        Session session = this.getSession(sessionProvider);
        Node rootNode = session.getRootNode();
        if(rootNode != null && rootNode.hasNode(LOGIN_HISTORY_HOME)) {
          Node homeNode = rootNode.getNode(LOGIN_HISTORY_HOME);
          if(homeNode != null && homeNode.hasNode(userId)){
            cloudUserNode = homeNode.getNode(userId);
          }
        }
      }
      catch (Exception e) {
        logger.error("Cloud Login: Error while getting cloud login status for user '" + userId + "'", e);
      }
    }
    
    return cloudUserNode;
  }
  
  /**
   * 
   * @param user
   * @return true if node exists
   */
  private boolean hasCloudLoginMixin(String userId) {
    boolean isExists = false;
    
    Node cloudUserNode = getCloudUserNode(userId);
    if(cloudUserNode != null) {
      try {
        // Fetch all mixin nodeTypes to search cloud mixin type
        for(NodeType nodeType : cloudUserNode.getMixinNodeTypes()) {
          if(nodeType.getName().equals(CL_MIXIN_TYPE)) {
            isExists = true;
            break;
          }
        }
      } 
      catch (RepositoryException e) {
        logger.error("Cloud Login: Error while getting cloud mixin node type for user '" + userId + "'", e);
      }
    }
    
    return isExists;
  }
  
  /**
   * With an email, returns his domain
   * <p>
   * If email is not valid, return empty string
   * 
   * @param email
   * @return
   */
  protected String extractDomainFromEmail(String email) {
    String domain = "";
    
    if(email != null) {
      Matcher m = emailPattern.matcher(email);
      if(m.matches()) {
        String extDomain = m.group(2);
        if(extDomain != null) {
          domain = extDomain;
        }
      }
    }
    
    return domain;
  }
  
  /**
   * Get the node container of temp avatar node
   * @param userId
   * @return
   */
  private Node getAvatarRootNode() {
    Node rootAvatarNode = null;
    
    try {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      Session session = this.getSession(sessionProvider);
      Node rootNode = session.getRootNode();
      
      if(rootNode != null && rootNode.hasNode(CL_JCR_APP_NODE_NAME)) {
        Node appNode = rootNode.getNode(CL_JCR_APP_NODE_NAME);
        
        // Folder node
        if(! appNode.hasNode(CL_JCR_FOLDER_NAME)) {
          rootAvatarNode = appNode.addNode(CL_JCR_FOLDER_NAME, "nt:folder");
          rootAvatarNode.addMixin("mix:referenceable");
        }
        else {
          rootAvatarNode = appNode.getNode(CL_JCR_FOLDER_NAME);
        }
      }
    }
    catch(Exception e) {
      logger.error("CloudLoginWizard: Cannot get root avatar node", e);
    }
    
    return rootAvatarNode;
  }
  
  /**
   * Permits to create an URI path to display temp avatar image
   * @return
   */
  public static String getAvatarUriPath() {
    return CL_JCR_ROOT_NODE_PATH + "/" + CL_JCR_APP_NODE_NAME + "/" + CL_JCR_FOLDER_NAME;
  }
}
