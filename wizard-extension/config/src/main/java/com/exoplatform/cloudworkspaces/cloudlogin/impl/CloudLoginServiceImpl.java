package com.exoplatform.cloudworkspaces.cloudlogin.impl;

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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService;
import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * Service used to manage cloud login NODE
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
      finally {
        sessionProvider.close();
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
      finally {
        sessionProvider.close();
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
}
