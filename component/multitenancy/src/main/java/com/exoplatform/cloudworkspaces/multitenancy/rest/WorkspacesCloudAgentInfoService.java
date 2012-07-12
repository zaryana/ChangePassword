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

import static org.exoplatform.cloudmanagement.status.TenantInfoBuilder.online;

import org.exoplatform.cloudmanagement.determinant.TenantDeterminant;
import org.exoplatform.cloudmanagement.multitenancy.TemporaryTenantStateHolder;
import org.exoplatform.cloudmanagement.multitenancy.TenantNameResolver;
import org.exoplatform.cloudmanagement.multitenancy.TenantRepositoryService;
import org.exoplatform.cloudmanagement.rest.CloudAgentInfoService;
import org.exoplatform.cloudmanagement.status.TenantInfo;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.statistic.TenantAccessTimeStatisticCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cloud-agent/info-service")
public class WorkspacesCloudAgentInfoService extends CloudAgentInfoService {

  private static final Logger              LOG = LoggerFactory.getLogger(WorkspacesCloudAgentInfoService.class);

  private final RepositoryService          repositoryService;

  private final TemporaryTenantStateHolder temporaryTenantStateHolder;

  private final OrganizationService        orgService;

  public WorkspacesCloudAgentInfoService(RepositoryService repositoryService,
                                         TemporaryTenantStateHolder temporaryTenantStateHolder,
                                         OrganizationService organizationService,
                                         BackupManager backupManager) {
    super(repositoryService, temporaryTenantStateHolder, organizationService, backupManager);
    this.repositoryService = repositoryService;
    this.orgService = organizationService;
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
  @Path("users-list")
  @RolesAllowed("cloud-admin")
  public Map<String, Map<String, String>> getUsersList() throws Exception {
    Set<String> notAvailableTenants = new HashSet<String>();
    for (String tenant : temporaryTenantStateHolder.getCreatingTenants().keySet())
      notAvailableTenants.add(tenant);
    for (String tenant : temporaryTenantStateHolder.getStartingTenantState().keySet())
      notAvailableTenants.add(tenant);
    for (String tenant : temporaryTenantStateHolder.getStoppingTenants().keySet())
      notAvailableTenants.add(tenant);
    try {
      Map<String, Map<String, String>> userMapForServer = new HashMap<String, Map<String, String>>();

      List<RepositoryEntry> repos = repositoryService.getConfig().getRepositoryConfigurations();

      for (RepositoryEntry repo : repos) {
        if (!notAvailableTenants.contains(repo.getName())) {
          Map<String, String> userMapForTenant = new HashMap<String, String>();
          userMapForServer.put(repo.getName(), userMapForTenant);
          repositoryService.setCurrentRepositoryName(repo.getName());

          ListAccess<User> allUsers;
          allUsers = orgService.getUserHandler().findAllUsers();
          if (allUsers != null && allUsers.getSize() > 0) {
            User[] users = allUsers.load(0, allUsers.getSize());
            for (User user : users) {
              userMapForTenant.put(user.getUserName(), user.getEmail());
            }
          }
        }
      }
      return userMapForServer;
    } finally {
      if (repositoryService instanceof TenantRepositoryService) {
        ((TenantRepositoryService) repositoryService).resetCurrentRepository();
      } else {
        try {
          LOG.warn("Unable to reset current repository. "
              + "org.exoplatform.cloudmanagement.multitenancy.TenantRepositoryService.resetCurrentRepository()"
              + " should be used");
          repositoryService.setCurrentRepositoryName(TenantNameResolver.getDefaultTenantRepositoryName());
        } catch (RepositoryConfigurationException rce) {
          LOG.error("Impossible to set current repository name", rce);
        }

      }
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("statistics")
  @RolesAllowed("cloud-admin")
  public Collection<TenantInfo> getTenantStatistic() {
    Map<String, TenantInfo> result = new HashMap<String, TenantInfo>();

    Map<String, TenantInfo> creatingTenants = temporaryTenantStateHolder.getCreatingTenants();
    for (Entry<String, TenantInfo> entry : creatingTenants.entrySet()) {
      String tenantName = entry.getKey();
      TenantInfo tenantInfo = entry.getValue();
      tenantInfo.setState(TenantState.CREATION);
      result.put(tenantName, tenantInfo);
    }

    Map<String, TenantInfo> startingTenants = temporaryTenantStateHolder.getStartingTenantState();
    for (Entry<String, TenantInfo> entry : startingTenants.entrySet()) {
      String tenantName = entry.getKey();
      TenantInfo tenantInfo = entry.getValue();
      tenantInfo.setState(TenantState.RESUMING);
      result.put(tenantName, tenantInfo);
    }

    Map<String, TenantInfo> stoppingTenants = temporaryTenantStateHolder.getStoppingTenants();
    for (Entry<String, TenantInfo> entry : stoppingTenants.entrySet()) {
      String tenantName = entry.getKey();
      TenantInfo tenantInfo = entry.getValue();
      tenantInfo.setState(TenantState.SUSPENDING);
      result.put(tenantName, tenantInfo);
    }

    TenantAccessTimeStatisticCollector accessCollector = TenantAccessTimeStatisticCollector.getInstance();
    String defaultRepoName = repositoryService.getConfig().getDefaultRepositoryName();

    for (RepositoryEntry repositoryEntry : repositoryService.getConfig()
                                                            .getRepositoryConfigurations()) {
      // repository may be available before executing of all Tenant creation
      // plugins
      String repositoryName = repositoryEntry.getName();
      if (!defaultRepoName.equals(repositoryName)) {
        if (!result.containsKey(repositoryName)) {

          TenantInfo tenantInfo = online(repositoryName).info();
          result.put(repositoryName, tenantInfo);

          // get last access time
          if (defaultRepoName.equals(repositoryName)) {
            tenantInfo.setLastAccessTime(accessCollector.getAccessTime(TenantDeterminant.DEFAULT_TENANT_NAME));
            tenantInfo.setSuspendable(false);
          } else {
            tenantInfo.setLastAccessTime(accessCollector.getAccessTime(repositoryName));
            tenantInfo.setSuspendable(true);
          }
        }
      }
    }

    LOG.debug("getTenantStatistic result {}", result.values());

    return result.values();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("last-tenant-activity-time")
  @RolesAllowed("cloud-admin")
  public Map<String, Long> getTenantActivity() {
    return super.getTenantActivity();
  }

}
