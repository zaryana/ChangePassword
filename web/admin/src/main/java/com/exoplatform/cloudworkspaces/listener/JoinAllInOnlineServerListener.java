/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.exoplatform.cloudworkspaces.listener;

import com.exoplatform.cloudworkspaces.users.UsersManager;

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.status.ServerBecomeOnlineListener;
import com.exoplatform.cloud.status.TenantInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JoinAllInOnlineServerListener implements ServerBecomeOnlineListener {

  private static final Logger LOG = LoggerFactory.getLogger(JoinAllInOnlineServerListener.class);

  private final UsersManager  usersManager;

  public JoinAllInOnlineServerListener(UsersManager usersManager) {
    this.usersManager = usersManager;
  }

  @Override
  public void onServerBecomeOnline(String serverAlias, List<TenantInfo> currentTenantStates) {
    for (TenantInfo tenant : currentTenantStates) {
      try {
        usersManager.joinAll(tenant.getTenantName());
      } catch (CloudAdminException e) {
        LOG.error("Unable to autojoin user.", e);
      }
    }
  }

}
