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
package org.exoplatform.cloudmanagement.admin.tenant;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerState;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * TODO: remove this class with upgrade to cloud-management 1.1-M8 Server
 * selection algorithm with lowest load factor.
 */
public class WorkspacesLowestLoadFactorServerSelectionAlgorithm implements ServerSelectionAlgorithm
{

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesLowestLoadFactorServerSelectionAlgorithm.class);

   private final ApplicationServerStatusManager serverStatusManager;

   private final TenantInfoDataManager infoDataManager;

   private final ApplicationServerConfigurationManager serverConfigurationManager;

   public WorkspacesLowestLoadFactorServerSelectionAlgorithm(TenantInfoDataManager infoDataManager,
      ApplicationServerStatusManager serverStatusManager,
      ApplicationServerConfigurationManager serverConfigurationManager)
   {
      super();
      this.infoDataManager = infoDataManager;
      this.serverStatusManager = serverStatusManager;
      this.serverConfigurationManager = serverConfigurationManager;
   }

   @Override
   public synchronized Collection<ApplicationServerStatus> selectServers() throws CloudAdminException
   {
      List<ApplicationServerWithLoadFactor> onlineServers = new ArrayList<ApplicationServerWithLoadFactor>();
      for (ApplicationServerStatus as : serverStatusManager.getApplicationServerStatusMap().values())
      {
         String alias = as.getAlias();
         LOG.info("SelectServers from  {} state {}", alias, as.getServerState().toString());
         Configuration serverConfiguration = serverConfigurationManager.getConfiguration(alias);
         if (as.getServerState() == ApplicationServerState.ONLINE
            && !serverConfiguration.getBoolean(ApplicationServerConfiguration.ON_MAINTENANCE))
         {

            int onlineTenantNumber = infoDataManager.getSize(TenantInfoFieldName.PROPERTY_APSERVER_ALIAS, alias);

            int maxTenantNumber = serverConfiguration.getInt(ApplicationServerConfiguration.MAX_TENANTS_NUM_PARAMETER);
            if (onlineTenantNumber < maxTenantNumber)
            {
               double loadFactor = (double)onlineTenantNumber / maxTenantNumber;
               LOG.debug(
                  "AS  {} tenant online {} load factor {} max {} status {} ",
                  new String[]{alias, Integer.toString(onlineTenantNumber),
                     Double.toString(Math.round(loadFactor * 1000) / 1000.0), Double.toString(maxTenantNumber),
                     as.getServerState().toString()});

               onlineServers.add(new ApplicationServerWithLoadFactor(as, loadFactor));
            }
         }
      }
      Collections.sort(onlineServers);
      ArrayList<ApplicationServerStatus> result = new ArrayList<ApplicationServerStatus>();
      for (ApplicationServerWithLoadFactor server : onlineServers)
      {
         result.add(server.getApplicationServerStatus());
      }
      return result;
   }

   static class ApplicationServerWithLoadFactor implements Comparable<ApplicationServerWithLoadFactor>
   {

      private final ApplicationServerStatus applicationServerStatus;

      private final double loadFactor;

      public ApplicationServerWithLoadFactor(ApplicationServerStatus applicationServerStatus, double loadFactor)
      {
         this.applicationServerStatus = applicationServerStatus;
         this.loadFactor = loadFactor;
      }

      @Override
      public int compareTo(ApplicationServerWithLoadFactor o)
      {
         if (loadFactor < o.loadFactor)
         {
            return -1;
         }
         if (loadFactor > o.loadFactor)
         {
            return 1;
         }
         return 0;
      }

      public ApplicationServerStatus getApplicationServerStatus()
      {
         return applicationServerStatus;
      }

   }

}
