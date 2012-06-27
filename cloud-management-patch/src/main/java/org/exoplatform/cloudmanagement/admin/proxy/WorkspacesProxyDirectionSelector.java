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
package org.exoplatform.cloudmanagement.admin.proxy;

import static org.exoplatform.cloudmanagement.admin.status.ApplicationServerState.OFFLINE;
import static org.exoplatform.cloudmanagement.admin.status.ApplicationServerState.ONLINE;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerState;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.exoplatform.cloudworkspaces.patch.utils.WorkspacesErrorMailSenderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Select direction of http request to tenant depends on tenant state and server
 * state. See
 * https://wiki-int.exoplatform.org/display/exoproducts/Proxy+redirection
 * +depends+on+tenant+and+server+state
 * 
 */
public class WorkspacesProxyDirectionSelector
{

   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesProxyDirectionSelector.class);

   public static enum Direction {
      SKIP, ADMIN, MAINTENANCE, APPLICATION_SERVER;
   }

   public Direction getDirection(TenantState tenantState, ApplicationServerState serverState)
      throws CloudAdminException
   {
      Direction result = Direction.SKIP;
      switch (tenantState)
      {
         case UNKNOWN :
            result = Direction.MAINTENANCE;
            break;
         case VALIDATING_EMAIL :
         case EMAIL_CONFIRMED :
         case WAITING_CREATION :
            result = Direction.SKIP;
            if (serverState != null)
            {
               throw new CloudAdminException("Server state " + serverState.toString()
                  + " should not be defined for tenant state " + tenantState.toString());
            }
            break;
         case CREATION :
         case CREATION_FAIL :
            result = Direction.SKIP;
            if (serverState == null)
            {
               throw new CloudAdminException("Server state should be defined for tenant state "
                  + tenantState.toString());
            }
            break;
         case ONLINE :

            // Unusable states
            if (OFFLINE.equals(serverState))
            {
               result = Direction.MAINTENANCE;
            }
            // working states
            else if (ONLINE.equals(serverState))
            {
               result = Direction.APPLICATION_SERVER;
            }
            else if (ApplicationServerState.STOPPING.equals(serverState))
            {
               result = Direction.MAINTENANCE;
            }

            // Impossible states CREATION or STARTING
            else
            {

               result = Direction.SKIP;
               if (serverState == null)
               {
                  throw new CloudAdminException("Unexpected server state null for tenant state "
                     + tenantState.toString());
               }
               else
               {
                  throw new CloudAdminException("Unexpected server state " + serverState.toString()
                     + " for tenant state " + tenantState.toString());
               }

            }
            break;
         case SUSPENDING :
            /*
             * This is stub to avoid problem with reloading haroxy by NullPointerException. See CLDINT-618
             */
            if (serverState != null)
            {
               // normal mode
               if (ONLINE.equals(serverState) || OFFLINE.equals(serverState))
               {
                  result = Direction.ADMIN;
                  if (serverState != null)
                  {
                     throw new CloudAdminException("Server state " + serverState.toString()
                        + " should not be defined for tenant state " + tenantState.toString());
                  }
               }
               // Impossible states CREATION or STARTING
               else
               {
                  result = Direction.SKIP;
                  throw new CloudAdminException("Unexpected server state " + serverState.toString()
                     + " for tenant state " + tenantState.toString());
               }
            }
            else
            {
               result = Direction.SKIP;
               LOG.warn("Tenant " + tenantState.toString()
                  + " has not valid state! Tenants with SUSPENDING state must have apServerStatus property.");
               WorkspacesErrorMailSenderProvider
                  .sendErrorToAdminIfNew(
                     "CLDINT-618 " + tenantState.toString(),
                     "Tenant " + tenantState.toString() + " is broken. Needs help of administrator.",
                     "Tenant "
                        + tenantState.toString()
                        + " has status SUSPENDING but not have property apServerStatus, this means that this tenant doesn't work."
                        + " To fix this tenant, change tenant's state to SUSPENDED, and call /cloud-admin/tenant-service/reset-cache rest service");
            }
            break;
         case SUSPENDED :
            // normal mode
            if (serverState == null)
            {
               result = Direction.ADMIN;
            }
            else if (ONLINE.equals(serverState) || OFFLINE.equals(serverState))
            {
               result = Direction.ADMIN;
               throw new CloudAdminException("Server state " + serverState.toString()
                  + " should not be defined for tenant state " + tenantState.toString());
            }
            // Impossible states CREATION or STARTING
            else
            {
               result = Direction.SKIP;
               throw new CloudAdminException("Unexpected server state " + serverState.toString() + " for tenant state "
                  + tenantState.toString());
            }
            break;
         case RESUMING :
            // normal mode
            if (ONLINE.equals(serverState) || OFFLINE.equals(serverState))
            {
               result = Direction.ADMIN;
               if (serverState == null)
               {
                  throw new CloudAdminException("Server state  should be defined  for tenant state "
                     + tenantState.toString());
               }
            }
            // Impossible states CREATION or STARTING
            else
            {
               result = Direction.SKIP;

               if (serverState == null)
               {
                  throw new CloudAdminException("Unexpected server state null for tenant state "
                     + tenantState.toString());
               }
               else
               {
                  throw new CloudAdminException("Unexpected server state " + serverState.toString()
                     + " for tenant state " + tenantState.toString());
               }
            }
            break;
         default :
            throw new CloudAdminException("Unknown tenant state " + tenantState.toString());
      }
      return result;
   }
}
