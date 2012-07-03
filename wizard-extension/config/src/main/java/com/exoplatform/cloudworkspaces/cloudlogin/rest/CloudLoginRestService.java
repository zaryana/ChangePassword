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
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

@Path(CloudLoginRestService.WS_ROOT_PATH)
public class CloudLoginRestService implements ResourceContainer {
  
  private static Log logger = ExoLogger.getLogger(CloudLoginRestService.class);

  protected final static String WS_ROOT_PATH = "/cloudlogin";
  public final static Long MAX_AVATAR_LENGTH = 2000000L;
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
  
  protected UploadService uploadService;
  
  public CloudLoginRestService() {
    uploadService = WCMCoreUtils.getService(UploadService.class);
  }
  
  /**
   * Method wich permit to update avatar image. 
   * <p>
   * <b>WARNING</b> An image need to be uploaded into server before call this method.
   * 
   * @param fileName name of the file uploaded into server
   * @param uploadId ID of current upload
   * @return JSON response
   * @throws Exception
   */
  @GET
  @Path("/setavatar")
  @RolesAllowed("users")
  public Response createAvatar(
                @QueryParam("fileName") String fileName,
                @QueryParam("uploadId") String uploadId) {

    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    
    if(fileName != null && uploadId != null) {
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      
      try {
        
        // Get resource uploaded into server
        UploadResource resource = uploadService.getUploadResource(uploadId);
        File file = new File(resource.getStoreLocation());
        FileInputStream inputStream = new FileInputStream(file);
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
        
        // Create avatar attachement
        AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream, 200, 0, null, fileName, mimeType, null);
        if(avatarAttachment == null) {
          avatarAttachment = new AvatarAttachment(null, fileName, resource.getMimeType(), inputStream, null, System.currentTimeMillis());
        }
        
        // Update profile with new avatar
        Profile p = Utils.getUserIdentity(userId, true).getProfile();
        p.setProperty(Profile.AVATAR, avatarAttachment);
        Map<String, Object> props = p.getProperties();
        // Removes avatar url and resized avatar
        for (String key : props.keySet()) {
          if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
            p.removeProperty(key);
          }
        }
        Utils.getIdentityManager().updateProfile(p);
      }
      catch(Exception e) {
        logger.error("WS " + WS_ROOT_PATH + "/setavatar has a problem with getting node of avatar", e);
        return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
      }
      finally {
        uploadService.removeUploadResource(uploadId);
      }
    }
    
    return Response.ok().cacheControl(cacheControl).build();
  }
}
