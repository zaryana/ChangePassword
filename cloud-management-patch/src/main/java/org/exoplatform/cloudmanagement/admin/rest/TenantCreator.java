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
package org.exoplatform.cloudmanagement.admin.rest;

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_TEMPLATE;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_TENANT_BACKUP_ID;
import static org.exoplatform.cloudmanagement.rest.admin.CloudAdminRestServicePaths.CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantMetadataValidator;
import org.exoplatform.cloudmanagement.admin.TenantRegistrationException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.creation.TenantCreationSupervisor;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.exoplatform.cloudmanagement.status.TenantStatus;
import org.exoplatform.cloudmanagement.status.TransientTenantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Tenant creation service for public use with email authorization.
 * This is a full copy of TenantCreatorWithEmailAuthorization from CM, but with shared adminConfiguration field for extensioning.
 */

@Path(CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE)
public class TenantCreator
{
   private static final Logger LOG = LoggerFactory.getLogger(TenantCreatorWithEmailAuthorization.class);

   protected final CloudAdminConfiguration adminConfiguration;

   protected final CloudInfoHolder cloudInfoHolder;

   private final TenantMetadataValidator tenantMetadataValidator;

   private final TenantCreationSupervisor creationSupervisor;

   public TenantCreator(CloudInfoHolder cloudInfoHolder,
      TenantMetadataValidator tenantMetadataValidator, CloudAdminConfiguration cloudAdminConfiguration,
      TenantCreationSupervisor creationSupervisor)
   {
      super();
      this.cloudInfoHolder = cloudInfoHolder;
      this.tenantMetadataValidator = tenantMetadataValidator;
      this.adminConfiguration = cloudAdminConfiguration;
      this.creationSupervisor = creationSupervisor;
   }

   @POST
   @Path("/create-with-confirm/{tenantname}/{user-mail}")
   public Response createTenantWithEmailConfirmation(@PathParam("tenantname") String tenantName,
      @PathParam("user-mail") String userMail) throws CloudAdminException
   {
      LOG.info("Received tenant creation request for {} from {}", tenantName, userMail);
      TransientTenantStatus tenantStatus = new TransientTenantStatus(tenantName);
      tenantStatus.setProperty(TenantStatus.PROPERTY_USER_MAIL, userMail);
      tenantStatus.setProperty(TenantStatus.PROPERTY_TEMPLATE_ID,
         adminConfiguration.getProperty(CLOUD_ADMIN_TENANT_BACKUP_ID));

      tenantMetadataValidator.validate(tenantStatus);
      cloudInfoHolder.updateTenantState(tenantStatus, TenantState.UNKNOWN, TenantState.VALIDATING_EMAIL);

      //send email
      String mailTemplate = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_CONFIRMATION_TEMPLATE, null);
      if (mailTemplate == null)
      {
         throw new TenantRegistrationException(500, "Mail template configuration not found. Please contact support.");
      }

      Map<String, String> props = new HashMap<String, String>();
      props.put("tenant.masterhost", adminConfiguration.getMasterHost());
      props.put("tenant.name", tenantStatus.getTenantName());
      props.put("user.mail", userMail);
      props.put("id", tenantStatus.getUuid());

      WorkspacesMailSender mailSender = new WorkspacesMailSender(adminConfiguration);

      mailSender.sendMail(userMail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_CONFIRMATION_SUBJECT),
         mailTemplate, props, false);

      return Response.ok(tenantStatus.getUuid()).build();
   }

   @POST
   @Path("/create-confirmed")
   public Response createTenantWithConfirmedEmail(@QueryParam("id") String uuid) throws CloudAdminException
   {
      LOG.info("Received  tenant creation request  with id {}", uuid);

      if (!cloudInfoHolder.isTenantValidationQueueExists(uuid))
      {
         LOG.warn("Id {} unknown", uuid);
         return Response.status(Status.BAD_REQUEST)
            .entity("Your confirmation key is wrong or has already been activated").build();
      }

      TenantStatus tenantStatus = cloudInfoHolder.getTenantFromValidationQueue(uuid);
      tenantMetadataValidator.validate(tenantStatus);
      cloudInfoHolder.updateValidationTenantState(uuid, TenantState.EMAIL_CONFIRMED);

      cloudInfoHolder.updateTenantState(tenantStatus.asTransient(), TenantState.EMAIL_CONFIRMED,
         TenantState.WAITING_CREATION);

      //creationSupervisor.createTenant(tenantStatus);

      return Response.ok().build();
   }
}
