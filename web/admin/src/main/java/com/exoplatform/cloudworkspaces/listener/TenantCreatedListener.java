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

import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.status.AbstractTenantStateListener;
import com.exoplatform.cloud.admin.tenant.TenantStateDataManager;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.users.UsersManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TenantCreatedListener extends AbstractTenantStateListener {

  private static final Logger          LOG = LoggerFactory.getLogger(TenantCreatedListener.class);

  private final NotificationMailSender notificationMailSender;

  private final UsersManager           usersManager;

  /**
   * @param tenantStateDataManager   TenantStateDataManager
   * @param notificationMailSender   NotificationMailSender
   * @param usersManager UsersManager
   */
  public TenantCreatedListener(TenantStateDataManager tenantStateDataManager,
                               NotificationMailSender notificationMailSender,
                               UsersManager usersManager) {
    super(tenantStateDataManager);
    this.notificationMailSender = notificationMailSender;
    this.usersManager = usersManager;
  }

  @Override
  public void onTenantCreated(String tenantName, Map<String, String> agentParam) {
    doAutoJoin(tenantName);
  }

  @Override
  public void onTenantStarted(String tenantName) {
    doAutoJoin(tenantName);
  }

  private void doAutoJoin(String tenantName) {
    try {
      usersManager.joinAll(tenantName, RequestState.WAITING_JOIN);
    } catch (CloudAdminException e) {
      LOG.error("Unable to join users to tenant " + tenantName, e);
      notificationMailSender.sendAdminErrorEmail("Unable to join users to tenant " + tenantName, e);
    }
  }

}
