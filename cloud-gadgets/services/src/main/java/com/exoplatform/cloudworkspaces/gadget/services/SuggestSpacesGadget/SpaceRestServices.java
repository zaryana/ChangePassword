package com.exoplatform.cloudworkspaces.gadget.services.SuggestSpacesGadget;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

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

import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.profile.ProfileFilter;



@Path("/gadgets/spaces/")
@Produces("application/json")
public class SpaceRestServices implements ResourceContainer {

  private static Log log = ExoLogger.getLogger(SpaceRestServices.class);

  private static final CacheControl cacheControl;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
  }

  @GET
  @Path("invitations")
  public Response getInvitations(@Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
    SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);    
    List<Space> invitedSpaces = spaceService.getInvitedSpaces(userId);
   
    JSONArray jsonArray = new JSONArray();
   
    for (Space space : invitedSpaces) {
  
      String avatar = space.getAvatarUrl();
      if (avatar == null) {avatar = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif"; }
       
      JSONObject json = new JSONObject();                    
      json.put("name", space.getName());
      json.put("displayName", space.getDisplayName());
      json.put("type", space.getType());
      json.put("spaceUrl", space.getUrl());   
      json.put("avatarUrl", avatar);  
      json.put("spaceId", space.getId());     
      jsonArray.put(json);    
    }

    return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    
    }
      catch (Exception e) {
        log.error("Error in space invitation rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
 
 
  @GET
  @Path("suggestions")
  public Response getSuggestions(@Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
    SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);   
    List<Space> suggestedSpaces = spaceService.getPublicSpaces(userId);
    
    
    JSONArray jsonArray = new JSONArray();
   
    for (Space space : suggestedSpaces) {
      
      if(space.getVisibility() == "hidden")
       continue;
      if(space.getRegistration() == "close")
       continue;

      String avatar = space.getAvatarUrl();
      if (avatar == null) {avatar = "/social-resources/skin/ShareImages/SpaceImages/SpaceLogoDefault_61x61.gif"; }      
        
      JSONObject json = new JSONObject();                    
      json.put("name", space.getName());
      json.put("spaceId", space.getId()); 
      json.put("displayName", space.getDisplayName());
      json.put("spaceUrl", space.getUrl());   
      json.put("avatarUrl", avatar);
      json.put("registration", space.getRegistration());
      json.put("members", space.getMembers().length);   
      jsonArray.put(json);    
    }

    return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    
    }
      catch (Exception e) {
        log.error("Error in space invitation rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
  @GET
  @Path("accept/{spaceName}")
  public Response accept(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
        SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);   
        
        if(spaceService.isInvitedUser(spaceService.getSpaceById(spaceName), userId))
            spaceService.addMember(spaceService.getSpaceById(spaceName), userId);
 
        return Response.ok("Accepted", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    
    }
      catch (Exception e) {
        log.error("Error in space accept rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
  @GET
  @Path("deny/{spaceName}")
  public Response deny(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
        SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);           
        spaceService.removeInvitedUser(spaceService.getSpaceById(spaceName), userId);
 
        return Response.ok("Deny", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();   
    }
      catch (Exception e) {
        log.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
  @GET
  @Path("request/{spaceName}")
  public Response request(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
        SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);           
        spaceService.addPendingUser(spaceService.getSpaceById(spaceName), userId);
 
        return Response.ok("Request", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();  
    }
      catch (Exception e) {
        log.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
    @GET
  @Path("join/{spaceName}")
  public Response join(@PathParam("spaceName") String spaceName, @Context SecurityContext sc, @Context UriInfo uriInfo) {
    
    try {
    
    String userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    
        SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);           
        if(spaceService.getSpaceById(spaceName).getRegistration().equals("open"))
            spaceService.addMember(spaceService.getSpaceById(spaceName), userId);
            
         
        return Response.ok("Join", MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();  
    }
      catch (Exception e) {
        log.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
  }
  
      @GET
    @Path("myspaces")
    public Response request(@Context SecurityContext sc, @Context UriInfo uriInfo) {
      
      try {
        
        String userId = getUserId(sc, uriInfo);
        if(userId == null) {
          return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
        }
        
        SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
        List<Space> mySpaces = spaceService.getAccessibleSpaces(userId);
        
        JSONArray jsonArray = new JSONArray();
      
        for (Space space : mySpaces) {                     
            JSONObject json = new JSONObject();                    
            json.put("name", space.getName());
            json.put("spaceId", space.getId()); 
            json.put("displayName", space.getDisplayName());
            json.put("spaceUrl", space.getUrl());   
            json.put("members", space.getMembers().length);   
            jsonArray.put(json);    
      }
        
      return Response.ok(jsonArray.toString(), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
        
      }
      catch (Exception e) {
        log.error("Error in space deny rest service: " + e.getMessage(), e);
        return Response.ok("error").cacheControl(cacheControl).build();
      }
   }

  @GET
  @Path("public")
  public Response getPublicSpaces(@Context SecurityContext sc, @Context UriInfo uriInfo) {
    String userId;
    userId = getUserId(sc, uriInfo);
    if(userId == null) {
        return Response.status(500).cacheControl(cacheControl).build();
    }
    try {
      SpaceService spaceService = (SpaceService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
      ListAccess publicSpaces = spaceService.getPublicSpacesWithListAccess(userId);
      JSONArray jsonArray = new JSONArray();
      Space spaces[] = (Space[])publicSpaces.load(0, publicSpaces.getSize());
      if(spaces != null && spaces.length > 0) {
        for(int i = 0; i < spaces.length; i++) {
          Space space = spaces[i];
          if(!space.getVisibility().equals("hidden") && !space.getRegistration().equals("close")) {
            JSONObject json = new JSONObject();
            json.put("name", space.getName());
            json.put("displayName", space.getDisplayName());
            json.put("spaceId", space.getId());
            jsonArray.put(json);
          }
        }
      }
      return Response.ok(jsonArray.toString(), "application/json").cacheControl(cacheControl).build();
    }
    catch(Exception e) {
      log.error((new StringBuilder()).append("Error in space invitation rest service: ").append(e.getMessage()).toString(), e);
    }
    return Response.ok("error").cacheControl(cacheControl).build();
  }

  
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