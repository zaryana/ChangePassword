/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.NotificationMailSender;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;
import com.exoplatform.cloudworkspaces.users.UserLimitsStorage;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantCreatedListenerThread implements Runnable {

  private static final Logger                    LOG                          = LoggerFactory.getLogger(TenantCreatedListenerThread.class);

  private static final String                    CLOUD_ADMIN_CREATION_TIMEOUT = "cloud.admin.tenant.creation.timeout";

  private String                                 tName;

  private TenantInfoDataManager                  tenantInfoDataManager;

  private WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private NotificationMailSender                 notificationMailSender;

  private UserLimitsStorage                      userLimitsStorage;

  private UserRequestDAO                         requestDao;

  private int                                    interval                     = 15000;

  private Configuration                          cloudAdminConfiguration;

  public TenantCreatedListenerThread(String tName,
                                     TenantInfoDataManager tenantInfoDataManager,
                                     WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                                     NotificationMailSender notificationMailSender,
                                     UserLimitsStorage userLimitsStorage,
                                     Configuration cloudAdminConfiguration,
                                     UserRequestDAO requestDao) {
    this.tName = tName;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.notificationMailSender = notificationMailSender;
    this.userLimitsStorage = userLimitsStorage;
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.requestDao = requestDao;
  }

  @Override
  public void run() {
    CloudIntranetUtils utils = new CloudIntranetUtils(cloudAdminConfiguration,
                                                      tenantInfoDataManager,
                                                      workspacesOrganizationRequestPerformer,
                                                      notificationMailSender,
                                                      userLimitsStorage,
                                                      requestDao);
    long limit = Integer.parseInt(cloudAdminConfiguration.getString(CLOUD_ADMIN_CREATION_TIMEOUT,
                                                                    "86400")) * 1000; // in
                                                                                      // milliseconds

    try {
      if (tenantInfoDataManager.isExists(tName)) {
        long count = 0;
        while (!tenantInfoDataManager.getValue(tName, TenantInfoFieldName.PROPERTY_STATE)
                                     .equals(TenantState.ONLINE.toString())) {
          if (count > limit) {
            notificationMailSender.sendAdminErrorEmail("Workspace " + tName + " creation timeout reached", null);
            throw new CloudAdminException("Workspace " + tName + " creation timeout reached");
          }
          Thread.sleep(interval);
          count += interval;
        }
        /*
         * Thread.sleep(interval * 20); // To let the proxy to reload;
         * 12.01.2012 // changed from 5min(20) to 30sec(2); // 17.01.2012 back
         * to 20
         */
        utils.joinAll(tName, RequestState.WAITING_JOIN);
      } else {
        LOG.error("Unable to find tenant '" + tName + "' in creation queue.");
      }
    } catch (InterruptedException e) {
      LOG.error("Unable to finish tenant '" + tName + "' creation", e);
      notificationMailSender.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
    } catch (CloudAdminException e) {
      LOG.error("Unable to finish tenant '" + tName + "' creation", e);
      notificationMailSender.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
    }
  }

}
