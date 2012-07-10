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

package com.exoplatform.cloudworkspaces.cloudlogin;

import org.exoplatform.upload.UploadResource;

import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

public interface CloudLoginService {

  /**
   * Returns the @CloudLoginStatus depending on the status of the JCR node
   * <p>
   * There is a specificity about status: IF there is no node created, the status is NONE
   * 
   * @param user
   * @return
   */
  public CloudLoginStatus getStatus(String userName);
  
  /**
   * Set the status to the cloud login node
   * 
   * @param user
   * @param status
   * @return
   */
  public CloudLoginStatus setStatus(String userName, CloudLoginStatus status);
  
  /**
   * Returns domain name of the current tenant
   * @return
   */
  public String getCloudTenantDomain();
  
  /**
   * This method create a JCR node which corresponds to an avatar image displayed temporarily by client.
   * Use AvatarAttachement to resize image
   * This node needs to be deleted after client use.
   * 
   * @return uri of image created
   */
  public String createTempAvatarNode(UploadResource upResource);
  
  /**
   * This method delete a JCR node which corresponds to an avatar image displayed temporarily by client.
   * 
   * @return
   */
  public void deleteTempAvatarNode();
  
  /**
   * Update the user profile with avatar resource, name and position
   * 
   * @param avatarResource
   */
  public void updateProfile(String userId, UploadResource avatarResource, String fullName, String position);
}
