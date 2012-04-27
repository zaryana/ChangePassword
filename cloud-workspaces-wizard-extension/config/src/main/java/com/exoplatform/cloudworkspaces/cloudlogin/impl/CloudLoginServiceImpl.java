package com.exoplatform.cloudworkspaces.cloudlogin.impl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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

  private static final String LOGIN_HISTORY_HOME = "exo:LoginHistoryHome";
  private static final String CL_MIXIN_TYPE = "exo:cloudlogin";
  private static final String CL_MIXIN_STATUS = "exo:cloudLoginStatus";

  
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

  public CloudLoginServiceImpl(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
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
}
