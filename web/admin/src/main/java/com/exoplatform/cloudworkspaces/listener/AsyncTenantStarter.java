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

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.tenant.TenantStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTenantStarter {

  private static final Logger   LOG = LoggerFactory.getLogger(AsyncTenantStarter.class);

  private final ExecutorService executorService;

  private final TenantStarter   tenantStarter;

  public AsyncTenantStarter(TenantStarter tenantStarter) {
    this.tenantStarter = tenantStarter;
    this.executorService = Executors.newFixedThreadPool(1);
  }

  public void startTenant(String tenantName) {
    executorService.execute(new StarterThread(tenantStarter, tenantName));
  }

  private class StarterThread implements Runnable {

    private final TenantStarter tenantStarter;

    private final String        tenantName;

    public StarterThread(TenantStarter tenantStarter, String tenantName) {
      this.tenantStarter = tenantStarter;
      this.tenantName = tenantName;
    }

    @Override
    public void run() {
      try {
        tenantStarter.startTenant(tenantName);
      } catch (CloudAdminException e) {
        LOG.error("Resuming of tenant " + tenantName + " was failed!", e);
      }
    }

  }

}
