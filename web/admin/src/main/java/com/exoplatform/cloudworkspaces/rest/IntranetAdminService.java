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

import com.exoplatform.cloudworkspaces.RequestState;

import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;

import com.exoplatform.cloudworkspaces.CloudIntranetUtils;
import com.exoplatform.cloudworkspaces.TenantCreatedListenerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.cloudmanagement.admin.queue.TenantQueueException;
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
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
         
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
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
         String username = userMail.substring(0, (userMail.indexOf("@")));
         tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't join you with an email address " + domain + ". Try with your work email.")
               .build();
         }
         // Prepare properties for mailing
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.repository.name", tName);
         props.put("user.mail", userMail);
         props.put("user.name", username);
         props.put("first.name", firstName);
         props.put("last.name", lastName);

         int maxUsers = utils.getMaxUsersForTenant(userMail);
         if (utils.isNewUserAllowed(tName, username, maxUsers))
         {
            // Storing user & sending appropriate mails
            utils.storeUser(tName, userMail, firstName, lastName, password, false);
            utils.sendUserJoinedEmails(tName, firstName, userMail, props);
         }
         else
         {
            // Limit reached
            props.put("users.maxallowed", Integer.toString(maxUsers));
            utils.sendJoinRejectedEmails(tName, userMail, props);
         }
         return Response.ok().build();
      }
      catch (UserAlreadyExistsException e)
      {
         return Response.ok(e.getMessage()).build();
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
    * @POST
    * @Path("/create")
    */
    public Response createIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("company-name") String companyName,
      @FormParam("phone") String phone, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
         if (!utils.validateEmail(userMail))
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
         String tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't create workspace with an email address " + domain + ". Try with your work email.")
               .build();
         }
         Response resp = super.createTenantWithConfirmedEmail(uuid);
         if (resp.getStatus() != 200)
            throw new CloudAdminException((String)resp.getEntity());
         TenantCreatedListenerThread thread =
            new TenantCreatedListenerThread(tName, userMail, firstName, lastName, companyName, phone, password,
               cloudInfoHolder, adminConfiguration);
         ExecutorService executor = Executors.newSingleThreadExecutor();
         executor.execute(thread);
         return Response.ok().build();
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
   public Response tenantStatus(@PathParam("tenantname") String tenantName)
   {
      try
      {
         TenantStatus status = cloudInfoHolder.getTenantStatus(tenantName);
         return Response.ok(status.getState().toString()).build();
      }
      catch (TenantQueueException e)
      {
         return Response.ok("NOT_FOUND").build();
      }
   }
   
   @POST
   @Path("/contactus")
   public Response contactUs(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("subject") String subject, @FormParam("text") String text)
   {
      utils.sendContactUsEmail(userMail, firstName, subject, text);
      return Response.ok().build();
   }
   
   
   
   @POST
   @Path("/create")
   public Response create(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("company-name") String companyName,
      @FormParam("phone") String phone, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      if (!utils.validateEmail(userMail))
         return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
      String tName = utils.getTenantNameFromWhitelist(userMail);
      if (tName == null)
      {
         String domain = userMail.substring(userMail.indexOf("@"));
         return Response.status(Status.BAD_REQUEST)
            .entity("Sorry, we can't create workspace with an email address " + domain + ". Try with your work email.")
            .build();
      }
      
      String folderName = utils.getRegistrationWaitingFolder();
      File folder = new File(folderName);
      if (!folder.exists())
         folder.mkdir();
      
      File[] list = folder.listFiles();
      for (File one : list)
      {
         if (one.getName().startsWith(tName + "_")){
            try
            {
               FileInputStream io = new FileInputStream(one);
               Properties newprops = new Properties();
               newprops.load(io);
               io.close();
               if (newprops.getProperty("user-mail").equalsIgnoreCase(userMail))
                  throw new CloudAdminException("Request to create a Cloud Workspace from " + userMail + " already submitted, it is on the processing currently. Wait for the creation will be done or use another email.");
            }
            catch (IOException e)
            {
            }
         }
      }
               
      File propertyFile = new File(folderName + tName + "_"+ System.currentTimeMillis() + ".properties");
      
      Properties properties = new Properties();
      properties.put("action", RequestState.WAITING_CREATION.toString());
      properties.put("tenant", tName);
      properties.put("user-mail", userMail);
      properties.put("first-name", firstName);
      properties.put("last-name", lastName);
      properties.put("company-name", companyName);
      properties.put("phone", phone);
      properties.put("password", password);
      properties.put("confirmation-id", uuid);
      
      try
      {
         propertyFile.createNewFile();
         properties.store(new FileOutputStream(propertyFile), "");
         LOG.info("Tenant " + tName + " put in creation queue. Requestor: " + userMail);
      }
      catch (Exception e)
      {
         LOG.error(e.getMessage());
         throw new CloudAdminException("A problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      
      return Response.ok().build();
   }
   
   
   @GET
   @RolesAllowed("cloud-manager")
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/requests")
   public Map<String, String[]> getTenantRequests() throws CloudAdminException
   {
      Map<String, String[]> result = new HashMap<String, String[]>();
      String folder = utils.getRegistrationWaitingFolder();
      File[] list = new File(folder).listFiles();
      if (list == null)
         return result;
      for (File one : list)
      {
         try
         {
            FileInputStream io = new FileInputStream(one);
            Properties properties = new Properties();
            properties.load(io);
            io.close();
            if (!properties.getProperty("action").equalsIgnoreCase(RequestState.WAITING_CREATION.toString()))
               continue;
            String tName = properties.getProperty("tenant");
            String[] data = new String[5];
            data[0] = tName;
            data[1] = properties.getProperty("user-mail");
            data[2] = properties.getProperty("first-name") + " " + properties.getProperty("last-name");
            data[3] = properties.getProperty("company-name");
            data[4] = properties.getProperty("phone");
            result.put(one.getName().substring(0, one.getName().indexOf(".")), data);
         }
         catch (Exception e)
         {
            LOG.error(e.getMessage());
            throw new CloudAdminException("A problem happened during retrieving requests list . It was reported to developers. Please, try again later.");
         }
      }
      return result;
   }

   @GET
   @Path("/validate/{decision}/{filename}")
   @RolesAllowed("cloud-manager")
   public Response validate(@PathParam("decision") String decision, @PathParam("filename") String filename)
      throws CloudAdminException
   {

      String folderName  = utils.getRegistrationWaitingFolder();
      File propertyFile = new File(folderName + filename + ".properties");
      Properties properties;
      try
      {
         FileInputStream io = new FileInputStream(propertyFile);
         properties = new Properties();
         properties.load(io);
         io.close();
      }
      catch (Exception ex)
      {
         throw new CloudAdminException("Tenant data file not found on server anymore.");
      }

      if (decision.equalsIgnoreCase("accept"))
      {
         Response resp =
            createIntranet(properties.getProperty("user-mail"), properties.getProperty("first-name"),
               properties.getProperty("last-name"), properties.getProperty("company-name"),
               properties.getProperty("phone"), properties.getProperty("password"),
               properties.getProperty("confirmation-id"));

         if (resp.getStatus() == 200 && resp.getEntity() == null)
         {
            File[] list = new File(folderName).listFiles();
            for (File one : list)
            {
               if (one.getName().startsWith(properties.getProperty("tenant") + "_")){
                  try
                  {
                     FileInputStream io = new FileInputStream(one);
                     Properties newprops = new Properties();
                     newprops.load(io);
                     io.close();
                     newprops.put("action", RequestState.WAITING_JOIN.toString());
                     newprops.store(new FileOutputStream(one), "");
                  }
                  catch (IOException e)
                  {
                  }
               }
            }
            propertyFile.delete();
            return resp;
         }
         else
         {
            throw new CloudAdminException("Can not apply this operation. Please contact support.");
         }

      }
      else if (decision.equalsIgnoreCase("refuse"))
      {
         LOG.info("Tenant " + properties.getProperty("tenant") + " creation was refused.");
         propertyFile.delete();
         return Response.ok().build();
      }
      else if (decision.equalsIgnoreCase("blacklist"))
      {
         //TODO: real blacklist
         LOG.info("Tenant " + properties.getProperty("tenant") + " was blacklisted.");
         propertyFile.delete();
         return Response.ok().build();
      }
      else
      {
         throw new CloudAdminException("Unknown action.");
      }

   }

}
