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
package com.exoplatform.cloudworkspaces.multitenancy.rest;

import com.exoplatform.cloud.multitenancy.TemporaryTenantStateHolder;
import com.exoplatform.cloud.multitenancy.TenantRepositoryService;
import com.exoplatform.cloud.status.TenantInfo;
import com.exoplatform.platform.cloud.services.multitenancy.PlatformCloudAgentInfoService;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.organization.OrganizationService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cloud-agent/info-service")
public class WorkspacesCloudAgentInfoService extends PlatformCloudAgentInfoService {

  private final RepositoryService          repositoryService;

  private final TemporaryTenantStateHolder temporaryTenantStateHolder;

  public WorkspacesCloudAgentInfoService(TenantRepositoryService repositoryService,
                                         TemporaryTenantStateHolder temporaryTenantStateHolder,
                                         OrganizationService organizationService,
                                         BackupManager backupManager) {
    super(repositoryService, temporaryTenantStateHolder, backupManager);
    this.repositoryService = repositoryService;
    this.temporaryTenantStateHolder = temporaryTenantStateHolder;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("is-ready/{tenant}")
  public Response isReady(@PathParam("tenant") String tenant) {
    if (temporaryTenantStateHolder.getCreatingTenants().containsKey(tenant)
        || temporaryTenantStateHolder.getStartingTenantState().containsKey(tenant)
        || temporaryTenantStateHolder.getStoppingTenants().containsKey(tenant)) {
      return Response.ok(Boolean.FALSE.toString()).build();
    }

    try {
      if (repositoryService.getConfig().getRepositoryConfiguration(tenant) != null) {
        return Response.ok(Boolean.TRUE.toString()).build();
      } else {
        return Response.ok(Boolean.FALSE.toString()).build();
      }
    } catch (RepositoryConfigurationException e) {
      return Response.serverError()
                     .entity("Sorry, rest service processing failed. Please, contact support.")
                     .build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("template-list")
  @RolesAllowed("cloud-admin")
  public List<String> getTemplateList() {
    return super.getTemplateList();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("statistics")
  @RolesAllowed("cloud-admin")
  public Collection<TenantInfo> getTenantStatistic() {
    return super.getTenantStatistic();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("last-tenant-activity-time")
  @RolesAllowed("cloud-admin")
  public Map<String, Long> getTenantActivity() {
    return super.getTenantActivity();
  }

}
