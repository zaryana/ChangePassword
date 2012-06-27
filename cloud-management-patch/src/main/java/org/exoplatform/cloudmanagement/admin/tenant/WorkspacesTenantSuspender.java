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
package org.exoplatform.cloudmanagement.admin.tenant;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.MetaDataStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.exoplatform.cloudmanagement.admin.status.TenantLockMapHolder;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;

public class WorkspacesTenantSuspender extends TenantSuspender
{
   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesTenantSuspender.class);

   private final TenantStateDataManager stateDataManager;

   private final TenantInfoDataManager infoDataManager;

   private final ApplicationServerStatusManager serverStatusManager;

   private final MetaDataStorage metaDataStorage;

   private final AgentRequestPerformer requestPerformer;

   private final TenantLockMapHolder lockMapHolder;

   public WorkspacesTenantSuspender(TenantStateDataManager stateDataManager, TenantInfoDataManager infoDataManager,
      ApplicationServerStatusManager serverStatusManager, MetaDataStorage metaDataStorage,
      TenantLockMapHolder lockMapHolder, AgentRequestPerformer requestPerformer)
   {
      super(stateDataManager, infoDataManager, serverStatusManager, metaDataStorage, lockMapHolder, requestPerformer);
      this.stateDataManager = stateDataManager;
      this.infoDataManager = infoDataManager;
      this.serverStatusManager = serverStatusManager;
      this.metaDataStorage = metaDataStorage;
      this.lockMapHolder = lockMapHolder;
      this.requestPerformer = requestPerformer;
   }

   /**
    * Check if tenant can be suspend and suspend it.
    * 
    * @param tenantName
    * @throws CloudAdminException
    */
   @Override
   public void suspend(String tenantName) throws CloudAdminException
   {
      if (!infoDataManager.isExists(tenantName))
      {
         throw new CloudAdminException("Tenant with name " + tenantName + " not found.");
      }

      TenantState tenantState =
         TenantState.valueOf(infoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_STATE));
      if (!tenantState.equals(TenantState.ONLINE))
      {
         LOG.warn("Tenant {} has state {}. Suspending skipped...", tenantName, tenantState.toString());
         return;
      }
      LOG.info("suspending ... {}", tenantName);

      String suspendable = infoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_SUSPENDABLE);
      if (suspendable != null)
      {

         if (Boolean.valueOf(suspendable))
         {

            if (!tenantState.equals(TenantState.ONLINE))
            {
               throw new CloudAdminException("Tenant " + tenantName + " with state " + tenantState.toString()
                  + " couldn't be suspended.");
            }

            try
            {
               String asAlias = infoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_APSERVER_ALIAS);
               if (asAlias != null)
               {
                  ApplicationServerStatus appStatus = serverStatusManager.getApplicationServerStatus(asAlias);
                  if (appStatus != null)
                  {
                     // get lock for tenant
                     Lock tenantLock = lockMapHolder.getNamedLock(tenantName);
                     InputStream metadata = null;
                     try
                     {
                        tenantLock.lock();

                        // 1.set state and reconfigure haproxy
                        stateDataManager.suspendingStart(tenantName);
                        // 2. suspend tenant
                        metadata = requestPerformer.stopTenant(tenantName, asAlias);
                        metaDataStorage.write(tenantName, metadata);
                        // 3. save tenant status
                        stateDataManager.suspended(tenantName);
                     }
                     catch (IOException e)
                     {
                        throw new CloudAdminException(e.getLocalizedMessage(), e);
                     }

                     finally
                     {
                        tenantLock.unlock();
                        if (metadata != null)
                        {
                           try
                           {
                              metadata.close();
                           }
                           catch (IOException e)
                           {
                              throw new CloudAdminException(e.getLocalizedMessage(), e);
                           }
                        }
                     }
                  }
                  else
                  {
                     LOG.warn("Application server status not found for  {}", asAlias);
                  }
               }
               else
               {
                  LOG.warn("Application server alias not found for tenant {}", tenantName);
               }
            }
            catch (CloudAdminException e)
            {
               stateDataManager.suspendingFail(tenantName, e);
            }
         }
         else
         {
            LOG.warn("Tenant {} can't be suspended because its SUSPENDABLE property set to false", tenantName);
         }
      }
      else
      {
         LOG.warn("{} not found in state of tenant {}", TenantInfoFieldName.PROPERTY_SUSPENDABLE, tenantName);
      }

   }
}
