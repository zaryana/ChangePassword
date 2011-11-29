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
package com.exoplatform.cloudworkspaces.rest;

import static org.exoplatform.cloudmanagement.rest.admin.CloudAdminRestServicePaths.CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE;

import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.TenantCreatedListenerThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantMetadataValidator;
import org.exoplatform.cloudmanagement.admin.TenantAlreadyExistException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.creation.TenantCreationSupervisor;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE)
public class IntranetAdminService extends TenantCreator
{

   CloudIntranetUtils utils;

   private static final Logger LOG = LoggerFactory.getLogger(IntranetAdminService.class);

   public IntranetAdminService(CloudInfoHolder cloudInfoHolder, TenantMetadataValidator tenantMetadataValidator,
      CloudAdminConfiguration cloudAdminConfiguration, TenantCreationSupervisor creationSupervisor)
   {
      super(cloudInfoHolder, tenantMetadataValidator, cloudAdminConfiguration, creationSupervisor);
      this.utils = new CloudIntranetUtils(cloudAdminConfiguration);
   }

   /* (non-Javadoc)
    * @see org.exoplatform.cloudmanagement.admin.rest.TenantCreator#createTenantWithEmailConfirmation(java.lang.String, java.lang.String)
    */
   @Override
   @POST
   @Path("/create-with-confirm/{tenantname}/{user-mail}")
   public Response createTenantWithEmailConfirmation(@PathParam("tenantname") String tenantName,
      @PathParam("user-mail") String userMail) throws CloudAdminException
   {
      return Response.status(Status.FORBIDDEN).entity("It's forbidden to use this method").build();
   }

   /* (non-Javadoc)
    * @see org.exoplatform.cloudmanagement.admin.rest.TenantCreator#createTenantWithConfirmedEmail(java.lang.String)
    */
   @Override
   @POST
   @Path("/create-confirmed")
   public Response createTenantWithConfirmedEmail(@QueryParam("id") String uuid) throws CloudAdminException
   {
      return Response.status(Status.FORBIDDEN).entity("It's forbidden to use this method").build();
   }

   /**
    * Sign-up method. Result is email with creating or joining workspace instructions.
    * 
    * @param userMail
    * @return Response
    * @throws CloudAdminException
    */
   @POST
   @Path("/signup")
   public Response signupToIntranet(@FormParam("user-mail") String userMail) throws CloudAdminException
   {
      LOG.info("Received signup request from " + userMail);
      String tName = null;
      String username = null;
      try
      {
         if (!utils.validateEmail(userMail))
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email adress.").build();
         username = userMail.substring(0, (userMail.indexOf("@")));
         tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't sign you up with an email address " + domain + ". Try with your work email.")
               .build();
         }
         super.createTenantWithEmailConfirmation(tName, userMail);
      }
      catch (TenantAlreadyExistException ex)
      {
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.repository.name", tName);
         props.put("user.mail", userMail);

         try
         {
            int maxUsers = utils.getMaxUsersForTenant(userMail);
            if (utils.isNewUserAllowed(tName, username, maxUsers))
            {
               // send OK email
               utils.sendOkToJoinEmail(userMail, props);
            }
            else
            {
               // send not allowed mails
               props.put("users.maxallowed", Integer.toString(maxUsers));
               utils.sendJoinRejectedEmails(tName, userMail, props);
            }
         }
         catch (UserAlreadyExistsException e)
         {
            return Response.ok(e.getMessage()).build();
         }
         catch (CloudAdminException e)
         {
            LOG.error(e.getMessage());
            return Response.ok("Can not finish signup action. Please contact support.").build();
         }
      }
      catch (CloudAdminException e)
      {
         LOG.error(e.getMessage());
         return Response.ok("Can not finish signup action. Please contact support.").build();
      }
      return Response.ok().build();
   }

   /**
    * Join to workspace service.
    * 
    * @param userMail
    * @param firstName
    * @param lastName
    * @param password
    * @param uuid
    * @return Response
    * @throws CloudAdminException
    */
   @POST
   @Path("/join")
   public Response joinIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      String tName = null;
      try
      {
         if (!utils.validateEmail(userMail))
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email adress.").build();
         String username = userMail.substring(0, (userMail.indexOf("@")));
         tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't join you with an email address " + domain + ". Try with your work email.")
               .build();
         }
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.repository.name", tName);
         props.put("user.mail", userMail);
         props.put("user.name", username);
         props.put("first.name", firstName);

         int maxUsers = utils.getMaxUsersForTenant(userMail);
         if (utils.isNewUserAllowed(tName, username, maxUsers))
         {
            utils.storeUser(tName, userMail, firstName, lastName, password, false);
            utils.sendUserJoinedEmails(tName, firstName, userMail, props);
         }
         else
         {
            props.put("users.maxallowed", Integer.toString(maxUsers));
            utils.sendJoinRejectedEmails(tName, userMail, props);
         }
         return Response.ok().build();
      }
      catch (UserAlreadyExistsException e)
      {
         return Response.ok(e.getMessage()).build();
      }
      catch (CloudAdminException e)
      {
         LOG.error(e.getMessage());
         return Response.ok("Can not finish join action. Please contact support.").build();
      }

   }

   /**
    * Service for creating workspaces.
    * 
    * @param userMail
    * @param firstName
    * @param lastName
    * @param companyName
    * @param phone
    * @param password
    * @param uuid
    * @return Response
    * @throws CloudAdminException
    */
   @POST
   @Path("/create")
   public Response createIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("company-name") String companyName,
      @FormParam("phone") String phone, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      try
      {
         if (!utils.validateEmail(userMail))
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email adress.").build();
         String tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't create workspace with an email address " + domain + ". Try with your work email.")
               .build();
         }
         super.createTenantWithConfirmedEmail(uuid);
         TenantCreatedListenerThread thread =
            new TenantCreatedListenerThread(tName, userMail, firstName, lastName, companyName, phone, password,
               cloudInfoHolder, adminConfiguration);
         ExecutorService executor = Executors.newSingleThreadExecutor();
         executor.execute(thread);
         return Response.ok().build();
      }
      catch (CloudAdminException e)
      {
         LOG.error(e.getMessage());
         return Response.ok("Can not finish workspace creation. Please contact support.").build();
      }
   }

   /**
    * Retrieves status string of the given tenant. 
    * 
    * @param tenantName
    * @return Response
    * @throws CloudAdminException
    */
   @GET
   @Path("/status/{tenantname}")
   public Response tenantStatus(@PathParam("tenantname") String tenantName) throws CloudAdminException
   {
      try
      {
         TenantStatus status = cloudInfoHolder.getTenantStatus(tenantName);
         return Response.ok(status.getState().toString()).build();
      }
      catch (CloudAdminException e)
      {
         return Response.ok("NOT_FOUND").build();
      }
   }

}
