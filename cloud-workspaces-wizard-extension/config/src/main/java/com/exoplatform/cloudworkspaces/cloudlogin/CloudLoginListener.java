/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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