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
package com.exoplatform.cloudworkspaces;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import com.exoplatform.cloud.admin.CloudAdminException;
import com.exoplatform.cloud.admin.configuration.AdminConfiguration;
import com.exoplatform.cloud.admin.configuration.TenantInfoFieldName;
import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.http.HttpClientManager;
import com.exoplatform.cloud.admin.tenant.ServerSelectionAlgorithm;
import com.exoplatform.cloud.admin.tenant.TenantStarter;
import com.exoplatform.cloud.status.TenantState;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DemoTenantOnlineKeeper implements Startable {

  private static final Logger            LOG = LoggerFactory.getLogger(DemoTenantOnlineKeeper.class);

  private final Timer                    timer;

  private final Configuration            cloudAdminConfiguration;

  private final TenantStarter            tenantStarter;

  private final CloudIntranetUtils       cloudIntranetUtils;

  private final TenantInfoDataManager    tenantInfoDataManager;

  private final HttpClientManager        httpClientManager;

  private final ServerSelectionAlgorithm serverSelectionAlgorithm;

  public DemoTenantOnlineKeeper(Configuration cloudAdminConfiguration,
                                TenantStarter tenantStarter,
                                CloudIntranetUtils cloudIntranetUtils,
                                TenantInfoDataManager tenantInfoDataManager,
                                HttpClientManager httpClientManager,
                                ServerSelectionAlgorithm serverSelectionAlgorithm) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.tenantStarter = tenantStarter;
    this.cloudIntranetUtils = cloudIntranetUtils;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.timer = new Timer();
    this.httpClientManager = httpClientManager;
    this.serverSelectionAlgorithm = serverSelectionAlgorithm;
  }

  @Override
  public void start() {
    timer.schedule(new TimerTask() {

      @Override
      public void run() {
        try {
          String demoTenant = cloudIntranetUtils.getDemoTenantName();
          Map<String, String> status = tenantInfoDataManager.getKeyValues(demoTenant);
          if (status.isEmpty()) {
            LOG.warn("Demo tenant with name {} not found.", demoTenant);
          } else {
            TenantState state = TenantState.valueOf(status.get(TenantInfoFieldName.PROPERTY_STATE));
            if (state.equals(TenantState.SUSPENDED)) {
              if (serverSelectionAlgorithm.selectServers().size() > 0) {
                tenantStarter.startTenant(demoTenant);
              }
            } else {
              long lastAccess = Long.parseLong(status.get(TenantInfoFieldName.PROPERTY_LAST_ACCESS_TIME));
              long suspendTime = cloudAdminConfiguration.getLong(AdminConfiguration.CLOUD_ADMIN_SUSPEND_TIME);
              double ratio = (double) (System.currentTimeMillis() - lastAccess)
                  / (double) suspendTime;
              if (ratio > 0.5) {
                String alias = status.get(TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);
                HttpClient httpClient = httpClientManager.getHttpClient(alias);
                HttpGet request = new HttpGet("http://" + demoTenant + "."
                    + System.getProperty("tenant.masterhost"));
                HttpResponse response = null;
                try {
                  response = httpClient.execute(request);
                } catch (ClientProtocolException e) {
                  LOG.error("Error happened until sending request to demo tenant", e);
                } catch (IOException e) {
                  LOG.error("Error happened until sending request to demo tenant", e);
                } finally {
                  if (response != null)
                    try {
                      response.getEntity().getContent().close();
                    } catch (IllegalStateException e) {
                      LOG.error("Error happened until sending request to demo tenant", e);
                    } catch (IOException e) {
                      LOG.error("Error happened until sending request to demo tenant", e);
                    }
                }
              }
            }
          }
        } catch (CloudAdminException e) {
          LOG.error("Error while resuming demo tenant.", e);
        }
      }

    },
                   2 * 60 * 1000,
                   1 * 60 * 1000);
  }

  @Override
  public void stop() {
  }

}
