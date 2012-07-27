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

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

import com.exoplatform.cloudworkspaces.cloudlogin.data.CloudLoginStatus;

/**
 * This listener is called after user login. Verify cloud login node to initiate or not cloud login process
 * 
 * @author Clement
 *
 */
public class CloudLoginListener extends Listener<ConversationRegistry, ConversationState> {

  private CloudLoginService cloudLoginService;

  public CloudLoginListener(CloudLoginService cloudLoginService) throws Exception {
    this.cloudLoginService = cloudLoginService;
  }

  /**
   * {@inheritDoc}
   */
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    String userId = event.getData().getIdentity().getUserId();
    
    CloudLoginStatus cloudLoginStatus = cloudLoginService.getStatus(userId);
    if(CloudLoginStatus.NONE.equals(cloudLoginStatus)) {
      cloudLoginService.setStatus(userId, CloudLoginStatus.INITIATED);
    }
  }
}