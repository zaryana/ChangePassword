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
package org.exoplatform.cloudmanagement.admin.rest;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantRemoveException;
import org.exoplatform.cloudmanagement.admin.dao.CachedTenantDataManager;
import org.exoplatform.cloudmanagement.admin.dao.MetaDataStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.TenantCreationRestarter;
import org.exoplatform.cloudmanagement.admin.tenant.TenantDisabler;
import org.exoplatform.cloudmanagement.admin.tenant.TenantRemover;
import org.exoplatform.cloudmanagement.admin.tenant.TenantStarter;
import org.exoplatform.cloudmanagement.admin.tenant.TenantSuspender;
import org.exoplatform.cloudworkspaces.patch.utils.WorkspacesErrorMailSenderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Set of services for changing state of tenant
 */
@Path("/cloud-admin/tenant-service")
public class WorkspacesTenantService
{
   private static final Logger LOG = LoggerFactory.getLogger(TenantService.class);

   private final TenantStarter tenantStarter;

   private final TenantSuspender tenantSuspender;

   private final TenantDisabler tenantDisabler;

   private final TenantInfoDataManager tenantInfoDataManager;

   private final TenantRemover tenantRemover;

   private final TenantCreationRestarter tenantCreationRestarter;

   private final MetaDataStorage metaDataStorage;

   public WorkspacesTenantService(TenantStarter tenantStarter, TenantSuspender tenantSuspender,
      TenantDisabler tenantDisabler, TenantRemover tenantRemover, TenantInfoDataManager tenantInfoDataManager,
      TenantCreationRestarter tenantCreationRestarter, MetaDataStorage metaDataStorage)
   {
      this.tenantStarter = tenantStarter;
      this.tenantSuspender = tenantSuspender;
      this.tenantDisabler = tenantDisabler;
      this.tenantRemover = tenantRemover;
      this.tenantInfoDataManager = tenantInfoDataManager;
      this.tenantCreationRestarter = tenantCreationRestarter;
      this.metaDataStorage = metaDataStorage;
   }

   /**
    * Restart tenant creation if it was failed. Available for tenants with
    * CREATION_FAIL state. Removes all initialized tenant resources (database,
    * local cloud-agent jcr resources) and marks tenant as WAITING_CREATION.
    * 
    * <table>
    * <tr>
    * <th>Status</th>
    * <th>Error description</th>
    * </tr>
    * <tr>
    * <td>400</td>
    * <td>tenant state is not available for this operation</td>
    * </tr>
    * <tr>
    * <td>500</td>
    * <td>tenant database can not be removed</td>
    * </tr>
    * <tr>
    * <td>500</td>
    * <td>can not clean tenant resources on application server, where creation
    * has failed</td>
    * </tr>
    * </table>
    * 
    * @param tenantName
    *           - name of tenant for restart creation
    * @return corresponding status 200
    * @throws CloudAdminException
    */
   @POST
   @Path("/restart-creation")
   @RolesAllowed("cloud-admin")
   public Response restartCreation(@QueryParam("tenant") String tenantName) throws CloudAdminException
   {
      tenantCreationRestarter.recreate(tenantName);
      return Response.ok().build();
   }

   /**
    * Executes starting of stopped tenant on available application server.
    * 
    * @param tenantName
    *           - tenant name for starting
    * @return Response with corresponded status 200
    * @throws CloudAdminException
    *            tenant doesn't exists or has incompatible for this operation
    *            state; no available space for tenant starting; tenant starting
    *            failed on agent server.
    */
   @GET
   @Path("/resume")
   @Produces("application/json")
   public Response startTenant(@QueryParam("tenant") String tenantName) throws CloudAdminException
   {
      try
      {
         LOG.debug("Start tenant {} ", tenantName);
         tenantStarter.startTenant(tenantName);
         return Response.ok().build();
      }
      catch (CloudAdminException e)
      {
         LOG.error(e.getMessage(), e);
         WorkspacesErrorMailSenderProvider.sendErrorToAdmin("Error while resuming tenant.", e);
         throw e;
      }
      catch (RuntimeException e)
      {
         LOG.error(e.getMessage(), e);
         WorkspacesErrorMailSenderProvider.sendErrorToAdmin("Error while resuming tenant.", e);
         throw e;
      }
   }

   /**
    * Stops ONLINE tenant.
    * 
    * @param tenantName
    *           - tenant name for resuming
    * @return Response with corresponded status 200
    * @throws CloudAdminException
    */
   @POST
   @Path("/suspend")
   @Produces("application/json")
   @RolesAllowed("cloud-admin")
   public Response stopTenant(@QueryParam("tenant") String tenantName) throws CloudAdminException
   {
      LOG.debug("Stop tenant {} ", tenantName);
      tenantSuspender.suspend(tenantName);
      return Response.ok().build();
   }

   /**
    * Make tenant unavailable for users.
    * 
    * @see org.exoplatform.cloudmanagement.admin.tenant.TenantDisabler#disable(java.lang.String)
    * 
    * @param tenantName
    *           - tenant name for disabling
    * @return Response with corresponded status 200
    * @throws CloudAdminException
    *            may occur on tenant suspending, or if tenant state wasn't
    *            satisfactory for this operation
    */
   @POST
   @Path("/disable")
   @RolesAllowed("cloud-admin")
   public Response disableTenant(@QueryParam("tenant") String tenantName) throws CloudAdminException
   {
      LOG.debug("Disable tenant {} ", tenantName);
      tenantDisabler.disable(tenantName);
      return Response.ok().build();
   }

   /**
    * Make disabled tenant available for users.
    * 
    * @see org.exoplatform.cloudmanagement.admin.tenant.TenantDisabler#enable(java.lang.String)
    * 
    * @param tenantName
    *           - tenant name for enabling
    * @return Response with corresponded status 200
    * @throws CloudAdminException
    *            occurs if tenant state wasn't satisfactory for this operation
    */
   @POST
   @Path("/enable")
   @RolesAllowed("cloud-admin")
   public Response enableTenant(@QueryParam("tenant") String tenantName) throws CloudAdminException
   {
      LOG.debug("Enable tenant {} ", tenantName);
      tenantDisabler.enable(tenantName);
      return Response.ok().build();
   }

   @POST
   @Path("/remove")
   @RolesAllowed("cloud-admin")
   public Response removeTenant(@QueryParam("tenant") String tenantName) throws TenantRemoveException
   {
      tenantRemover.remove(tenantName);
      return Response.ok().build();
   }

   /**
    * Get a state properties of selected tenant
    * 
    * @param tenant
    *           - name of selected tenant
    * @return - tenant state properties in Map representation
    * @throws org.exoplatform.cloudmanagement.admin.dao.TenantDataManagerException
    */
   @Path("/tenant-status")
   @GET
   @Produces("application/json")
   @RolesAllowed({"cloud-admin", "cloud-manager"})
   public Map<String, String> getTenantStatus(@QueryParam("tenant") String tenant) throws TenantDataManagerException
   {
      return tenantInfoDataManager.getKeyValues(tenant);
   }

   /**
    * Removes metadata for some tenant
    * 
    * @param tenant
    * @throws TenanDataManagerException
    */
   @Path("/metadata-remove")
   @POST
   @RolesAllowed("cloud-admin")
   public Response removeMetadata(@QueryParam("tenant") String tenant)
   {
      metaDataStorage.remove(tenant);
      return Response.ok().build();
   }

   /**
    * Reset tenant data manager cache and reload it from persistent storage. It
    * may be useful to reload changes made outside from cloud-admin in tenant
    * states. Typically design for cloud administrator, to give them ability to
    * made some changes in tenant states and notify cloud-admin about it.
    * 
    */
   @GET
   @Path("/reset-cache")
   @RolesAllowed("cloud-admin")
   public void resetCache() throws TenantDataManagerException
   {
      LOG.info("Admin going to reset tenant data manager cache and reload data from persistant storage");
      if (tenantInfoDataManager instanceof CachedTenantDataManager)
      {
         ((CachedTenantDataManager)tenantInfoDataManager).resetAndReloadFromPersistentDataManager();
         LOG.info("Reset complete");
      }
      else
      {
         LOG.warn("Wrong instance of tenantInfoDataManager expected {} but found ",
            CachedTenantDataManager.class.getName(), tenantInfoDataManager.getClass().getName());
      }
   }

}
