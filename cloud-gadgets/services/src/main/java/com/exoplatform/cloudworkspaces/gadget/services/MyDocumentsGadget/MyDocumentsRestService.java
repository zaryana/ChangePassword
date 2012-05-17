package com.exoplatform.cloudworkspaces.gadget.services.MyDocumentsGadget;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Context;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.utils.comparator.PropertyValueComparator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.cms.link.LinkManager;
import org.json.JSONObject;

/**
 * @author lamphan AUG 01, 2010
 */

@Path("/gadgets/mydocuments/")
@Produces("application/json")
public class MyDocumentsRestService implements ResourceContainer {


  private static final String DATE_MODIFIED   = "exo:dateModified";
  private static final String TITLE   = "exo:title";
  private static final String LAST_MODIFIED_PROPERTY = "Last-Modified";
  private static final String IF_MODIFIED_SINCE_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";  
  
  private static Log log = ExoLogger.getLogger(MyDocumentsRestService.class);

  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }
  
    
  @GET
  @Path("/all")
  public Response getFavoriteByUser(@Context SecurityContext sc, @Context UriInfo uriInfo) throws Exception {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
          return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }    
    
 
    try {
      
      List<Node> listNodes = new ArrayList<Node>();
      NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      SessionProviderService sessionProviderService = (SessionProviderService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);
        
      Node userNode = nodeHierarchyCreator.getUserNode(sessionProviderService.getSystemSessionProvider(null), userId);
      Node privateDrive = userNode.getNode("Private");
      Node publicDrive = userNode.getNode("Public");
      
          
      getListDocument(privateDrive, listNodes);
      getListDocument(publicDrive, listNodes);
      
      Collections.sort(listNodes, new PropertyValueComparator(DATE_MODIFIED, PropertyValueComparator.DESCENDING_ORDER));
            
      JSONArray jsonArray = new JSONArray();
      
      for (Node favorite : listNodes) {
        JSONObject json = new JSONObject(); 
        json.put("name", favorite.getName());  
        json.put("title", getTitle(favorite));      
        json.put("nodePath", favorite.getPath()); 
        json.put("linkImage", "Icon16x16 default16x16Icon" + getNodeTypeIcon(favorite, "16x16Icon"));
        jsonArray.put(json);
      }
      
      return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    }  
    catch (Exception e) {
        log.error("Error in my document gadget rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }

  }
  
  @GET
  @Path("/path")
  public Response getFolders(@Context SecurityContext sc, @Context UriInfo uriInfo) throws Exception {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
          return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }      
    try {
      
      List<Node> listNodes = new ArrayList<Node>();
      NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      SessionProviderService sessionProviderService = (SessionProviderService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);      
      Node userNode = nodeHierarchyCreator.getUserNode(sessionProviderService.getSystemSessionProvider(null), userId);
      Node privateDrive = userNode.getNode("Private");
      Node publicDrive = userNode.getNode("Public");
      
      JSONObject jsonPath = new JSONObject();
      jsonPath.put("private", privateDrive.getPath());
      jsonPath.put("public", publicDrive.getPath());
      
      return Response.ok(jsonPath.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
      
    } catch (Exception e) {
        log.error("Error in document path rest service: " + e.getMessage(), e);
        return Response.ok("error " + e).cacheControl(cacheControl).build();
      }

  }

  
  public static String getNodeTypeIcon(Node node, String appended) throws RepositoryException {
      StringBuilder str = new StringBuilder();
      
      if (node == null) return "";
      
      String nodeType = node.getPrimaryNodeType().getName();
      
      if (node.isNodeType("exo:symlink")) {
        LinkManager linkManager = (LinkManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LinkManager.class);
        try {
          nodeType = node.getProperty("exo:primaryType").getString();
          node = linkManager.getTarget(node);
          if (node == null)
            return "";
        } catch (Exception e) {
          return "";
        }
      }
      
      nodeType = nodeType.replace(':', '_') + appended;
      str.append(nodeType);
      str.append(" ");
      str.append("default16x16Icon");
      if (node.isNodeType("nt:file")) {
        if (node.hasNode("jcr:content")) {
          Node jcrContentNode = node.getNode("jcr:content");
          str.append(' ').append(
              jcrContentNode.getProperty("jcr:mimeType").getString().replaceAll(
                  "/|\\.", "_")).append(appended);
        }
      }
      return str.toString();
    }
  
  public void getListDocument(Node node, List<Node> listNodes) throws RepositoryException {
      LinkManager linkManager = (LinkManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LinkManager.class);
      String primaryType = node.getProperty("jcr:primaryType").getString();
      if (primaryType.contains("nt:file") || linkManager.isLink(node)) {
          listNodes.add(node);
          return;
          }
      else
      {
        if (node.hasNodes()){
                NodeIterator childNodes = node.getNodes();
                  while (childNodes.hasNext()) {
                          Node childNode = childNodes.nextNode();
                          getListDocument(childNode, listNodes);            
                  }
        }
      }
  }
  
  public List<Node> getAllFavoritesByUser(String workspace, String repository, Node userNode) throws Exception {
      List<Node> ret = new ArrayList<Node>();
      LinkManager linkManager = (LinkManager)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LinkManager.class);
      NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(NodeHierarchyCreator.class);
      String favoritePath = nodeHierarchyCreator.getJcrPath("userPrivateFavorites");
      Node favoriteFolder= userNode.getNode(favoritePath);
        
      NodeIterator nodeIter = favoriteFolder.getNodes();
      while (nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode();
        String primaryType = childNode.getProperty("jcr:primaryType").getString();
        if (linkManager.isLink(childNode) || primaryType.contains("nt:file")) {
          ret.add(childNode);
        }
      }
      return ret;
    }
  


  private String getTitle(Node node) throws Exception {
    if (node.hasProperty(TITLE))
      return node.getProperty(TITLE).getString();
    return node.getName();
  }

  private String getDateFormat(Calendar date) {
    return String.valueOf(date.getTimeInMillis());
  }

/*
  private String getDriveName(List<DriveData> listDrive, Node node) throws RepositoryException{
    String driveName = "";
    for (DriveData drive : listDrive) {
      if (node.getSession().getWorkspace().getName().equals(drive.getWorkspace())
          && node.getPath().contains(drive.getHomePath()) && drive.getHomePath().equals("/")) {
        driveName = drive.getName();
        break;
      }
    }
    return driveName;
  }*/
  
  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    try {
      return sc.getUserPrincipal().getName();
      } catch (NullPointerException e) {
        return getViewerId(uriInfo);
        } catch (Exception e) {
          return null;
        }
      }
      
      private String getViewerId(UriInfo uriInfo) {
        URI uri = uriInfo.getRequestUri();
        String requestString = uri.getQuery();
        if (requestString == null) return null;
        String[] queryParts = requestString.split("&");
        for (String queryPart : queryParts) {
          if (queryPart.startsWith("opensocial_viewer_id")) {
            return queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
          }
        }
        return null;
      }
  

}