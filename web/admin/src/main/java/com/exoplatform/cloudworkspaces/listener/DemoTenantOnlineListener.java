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

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.status.ServerBecomeOnlineListener;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DemoTenantOnlineListener implements ServerBecomeOnlineListener {

  private static final Logger         LOG = LoggerFactory.getLogger(DemoTenantOnlineListener.class);

  private final CloudIntranetUtils    utils;

  private final TenantInfoDataManager tenantInfoDataManager;

  public DemoTenantOnlineListener(CloudIntranetUtils utils,
                                  TenantInfoDataManager tenantInfoDataManager) {
    this.utils = utils;
    this.tenantInfoDataManager = tenantInfoDataManager;
  }

  @Override
  public void onServerBecomeOnline(String serverAlias, List<TenantInfo> currentTenantStates) {
    try {
      String demoTenant = utils.getDemoTenantName();
      Map<String, String> status = tenantInfoDataManager.getKeyValues(demoTenant);
      if (!status.isEmpty()) {
        TenantState state = TenantState.valueOf(status.get(TenantInfoFieldName.PROPERTY_STATE));
        if (state.equals(TenantState.ONLINE)) {
          tenantInfoDataManager.set(demoTenant,
                                    TenantInfoFieldName.PROPERTY_LAST_ACCESS_TIME,
                                    String.valueOf(System.currentTimeMillis()));
        }
      }
    } catch (CloudAdminException e) {
      LOG.error("Error happened in DemoTenantOnlineListener", e);
    }
  }
}
