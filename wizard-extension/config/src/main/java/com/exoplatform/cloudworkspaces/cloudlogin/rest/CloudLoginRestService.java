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

package com.exoplatform.cloudworkspaces.cloudlogin.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileUploadException;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import com.exoplatform.cloudworkspaces.cloudlogin.CloudLoginService;

/**
 * This class is used to contact some REST WS from the tool cloud login wizard.
 * 
 * @author Clement
 *
 */
@Path(CloudLoginRestService.WS_ROOT_PATH)
public class CloudLoginRestService implements ResourceContainer {
  
  private static Log logger = ExoLogger.getLogger(CloudLoginRestService.class);

  protected final static String WS_PORTAL_PATH = "/portal/rest";
  protected final static String WS_ROOT_PATH = "/cloudlogin";
  protected final static String WS_UPLOAD_PATH = "/uploadavatar";
  protected final static String WS_PROFILE_PATH = "/updateprofile";
  public final static String WS_UPLOAD_PARAM_UPLOAD_ID = "uploadId";
  
  public final static Integer MAX_AVATAR_LENGTH = 2000000;
  public final static String AVATAR_UPLOAD_ID = "cloudloginavatar";
  
  @SuppressWarnings("serial")
  private static final List<String> ACCEPTED_MIME_TYPES = new ArrayList<String>() {
    {
      add("image/jpeg");
      add("image/jpg");
      add("image/png");
      add("image/x-png");
      add("image/pjpeg");
    }
  };
  
  private UploadService uploadService;
  private CloudLoginService cloudLoginService;
  
  public CloudLoginRestService(CloudLoginService cloudLoginService) {
    uploadService = WCMCoreUtils.getService(UploadService.class);
    this.cloudLoginService = cloudLoginService;
  }
  
  /**
   * This method is used to upload an image into server, create a temporary thumbnails into JCR and returns URL of this image
   * 
   * @param servletRequest
   * @param uploadId
   * @return
   */
  @POST
  @Path(CloudLoginRestService.WS_UPLOAD_PATH)
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  public Response uploadAvatar(@Context HttpServletRequest servletRequest, @QueryParam(CloudLoginRestService.WS_UPLOAD_PARAM_UPLOAD_ID) String uploadId) {

    CacheControl cacheControl = new CacheControl();
    String avatarUri = "";

    if(uploadId != null && servletRequest != null) {
      try {
        uploadService.addUploadLimit(uploadId, MAX_AVATAR_LENGTH);
        uploadService.createUploadResource(servletRequest);
        
        // Try to create a JCR node to have a thumbnails displayable by client
        UploadResource upResource = uploadService.getUploadResource(uploadId);
        
        // Create Temporary avatar node
        cloudLoginService.createTempAvatarNode(upResource);
        
        // Manage cache
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);
      }
      catch (FileUploadException e) {
        logger.warn("Cannot upload image with id: " + uploadId + " (" + e.getMessage() + ")");
      }
      catch (Exception e) {
        logger.warn("Cannot upload image with id: " + uploadId + " (" + e.getMessage() + ")");
      }
    }
    
    return Response.ok(avatarUri).cacheControl(cacheControl).build();
  }
  
  /**
   * Method wich permit to update avatar image. 
   * <p>
   * <b>WARNING</b> An image need to be uploaded into server before call this method.
   * 
   * @param fileName name of the file uploaded into server
   * @param uploadId ID of current upload
   * @return 
   * @throws Exception
   */
  @GET
  @Path(CloudLoginRestService.WS_PROFILE_PATH)
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  public Response updateProfile(
                @QueryParam("fileName") String fileName,
                @QueryParam("uploadId") String uploadId,
                @QueryParam("firstName") String firstName,
                @QueryParam("lastName") String lastName,
                @QueryParam("position") String position) {

    boolean hasNewAvatar = (fileName != null && uploadId != null);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    
    try {
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      UploadResource resource = null;

      if(hasNewAvatar) {
        // Get resource uploaded into server
        resource = uploadService.getUploadResource(uploadId);
        if(resource != null) {
          File file = new File(resource.getStoreLocation());
          String mimeType = resource.getMimeType();
          
          if (! ACCEPTED_MIME_TYPES.contains(mimeType)) {
            uploadService.removeUploadResource(uploadId);
            logger.warn("Mimetype " + mimeType + " is not accepted" + mimeType);
            return Response.ok("Your file is not in proper format", MediaType.TEXT_PLAIN).cacheControl(cacheControl).build();
          }
          
          if(file.length() >= MAX_AVATAR_LENGTH) {
            uploadService.removeUploadResource(uploadId);
            logger.warn("File size is too large: " + file.length());
            return Response.ok("File size is too large: " + file.length() + " (Max=" + MAX_AVATAR_LENGTH + ")", MediaType.TEXT_PLAIN).cacheControl(cacheControl).build();
          }
        }
      }
      else {
        logger.warn("Upload resource with id " + uploadId + " cannot be found.");
      }
        
      // Update user profile
      cloudLoginService.updateProfile(userId, resource, firstName, lastName, position);

      if(hasNewAvatar) {
        // Delete temporary avatar
        cloudLoginService.deleteTempAvatarNode();
      }
    }
    catch(Exception e) {
      logger.error("WS " + WS_ROOT_PATH + "/setavatar has a problem with getting node of avatar", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
    finally {
      if(hasNewAvatar) {
        uploadService.removeUploadResource(uploadId);
      }
    }
    
    return Response.ok().cacheControl(cacheControl).build();
  }
  
  /**
   * Used to create a request to the WS method uploadAvatar
   * @return
   */
  public static String getUploadWsPath() {
    return WS_PORTAL_PATH + WS_ROOT_PATH + WS_UPLOAD_PATH;
  }
  
  /**
   * Used to create a request to the WS method updateProfile
   * @return
   */
  public static String getProfileWsPath() {
    return WS_PORTAL_PATH + WS_ROOT_PATH + WS_PROFILE_PATH;
  }
}
