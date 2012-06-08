/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.exoplatform.cloudworkspaces.multitenancy.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.jcr.RepositoryException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupChain;
import org.exoplatform.services.jcr.ext.backup.BackupConfig;
import org.exoplatform.services.jcr.ext.backup.BackupConfigurationException;
import org.exoplatform.services.jcr.ext.backup.BackupManager;
import org.exoplatform.services.jcr.ext.backup.BackupOperationException;
import org.exoplatform.services.jcr.ext.backup.RepositoryBackupChain;
import org.exoplatform.services.jcr.ext.backup.RepositoryBackupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for tenant template management and synchronization on agent side.
 * To get current list of templates available on the app server use agent's service:
 * <pre>/cloud-agent/info-service/template-list</pre>.
 * 
 * 
 */
@Path("/cloud-agent/template-service")
public class WorkspaceTenantTemplateService {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceTenantTemplateService.class);
  
  protected final BackupManager     jcrBackup;

  protected final RepositoryService jcrService;

  public WorkspaceTenantTemplateService(RepositoryService jcrService, BackupManager jcrBackup) {
    this.jcrService = jcrService;
    this.jcrBackup = jcrBackup;
  }
  
  /**
   * Create JCR backup on this app server. It's simplified version of HTTPBackupAgent dedicated for cloud goal. 
   * It returns Id of issued to creation template (JCR backup). After this call a client should use 
   * <pre>/cloud-agent/info-service/template-list</pre> to ensure the backup was created with no errors.
   * 
   * @return Response with Id of created template or error code.  
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("cloud-admin")
  @Path("/create-template")
  public Response createTemplate() {
    String repository = "";
    try {
      File backupDir = jcrBackup.getBackupDirectory();

      // this solution will works only if we have 'tenant.repository.name' thus and default repo, 
      // in case when we work w/o default repo, will need another solution
      repository = System.getProperty("tenant.repository.name") ; 
      try { 
        jcrService.getConfig().getRepositoryConfiguration(repository);
      } catch(RepositoryConfigurationException e) {
        // cannot read the repo, cannot do the work
        String error = "ERROR: Repository '" + repository + "' not found: " + e.getMessage();
        LOG.error(error, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
      }
      
      RepositoryBackupConfig config = new RepositoryBackupConfig();
      config.setBackupType(BackupManager.FULL_BACKUP_ONLY);
      config.setRepository(repository);
      config.setBackupDir(backupDir);
      config.setIncrementalJobPeriod(0);
      config.setIncrementalJobNumber(0);
      
      RepositoryBackupChain chain = jcrBackup.startBackup(config);
      
      String templateId = chain.getBackupId();
      LOG.info("Tenant template creation issued with Id " + templateId);
      List<String> templateList = new ArrayList<String>();
      templateList.add(templateId);
      
      return Response.ok().entity(templateList) .build(); 
    } catch (BackupOperationException e) {
      String error = "Cannot backup repository '" + repository + "': " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
    } catch (BackupConfigurationException e) {
      String error = "Backup configuration error for repository '" + repository + "': " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
    } catch (RepositoryConfigurationException e) {
      String error = "Repository '" + repository + "' configuration error: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
    } catch(RepositoryException e) {
      String error = "Repository '" + repository + "' error: " + e.getMessage();
      LOG.error(error, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
  }
  
  
}
