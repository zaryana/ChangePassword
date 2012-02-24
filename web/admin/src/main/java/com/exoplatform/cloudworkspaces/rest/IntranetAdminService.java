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

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_CONFIRMATION_TEMPLATE;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_TENANT_BACKUP_ID;
import static org.exoplatform.cloudmanagement.rest.admin.CloudAdminRestServicePaths.CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE;

import com.exoplatform.cloudworkspaces.ChangePasswordManager;

import com.exoplatform.cloudworkspaces.ReferencesManager;

import com.exoplatform.cloudworkspaces.listener.TenantCreatedListenerThread;
import com.exoplatform.cloudworkspaces.UserRequest;
import com.exoplatform.cloudworkspaces.UserRequestDAO;
import com.exoplatform.cloudworkspaces.RequestState;
import com.exoplatform.cloudworkspaces.UserAlreadyExistsException;
import com.exoplatform.cloudworkspaces.CloudIntranetUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.exoplatform.cloudmanagement.status.TenantState;
import org.exoplatform.cloudmanagement.status.TenantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE)
public class IntranetAdminService extends TenantCreator
{

   CloudIntranetUtils utils;

   UserRequestDAO requestDao;

   private static final Logger LOG = LoggerFactory.getLogger(IntranetAdminService.class);

   public IntranetAdminService(CloudInfoHolder cloudInfoHolder, TenantMetadataValidator tenantMetadataValidator,
      CloudAdminConfiguration cloudAdminConfiguration, TenantCreationSupervisor creationSupervisor)
   {
      super(cloudInfoHolder, tenantMetadataValidator, cloudAdminConfiguration, creationSupervisor);
      this.requestDao = new UserRequestDAO(cloudAdminConfiguration);
      this.utils = new CloudIntranetUtils(cloudAdminConfiguration, cloudInfoHolder, requestDao);

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
    * Sign-up the Cloud. Result is an email with instructions on creation or joining a tenant.
    * 
    * @param String userMail email address of user to signup
    * @return Response OK with details message or an error. 
    * @throws CloudAdminException if error occurs
    */
   @POST
   @Path("/signup")
   public Response signup(@FormParam("user-mail") String userMail) throws CloudAdminException
   {
      LOG.info("Received Signup request from " + userMail);
      String tName = null;
      String username = null;
      try
      {
         if (!utils.validateEmail(userMail)) 
         {
            LOG.info("User " + userMail + " rejected. Need valid email address.");
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();
         }

         username = userMail.substring(0, (userMail.indexOf("@")));
         tName = utils.getTenantNameFromWhitelist(userMail);
         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            LOG.info("User " + userMail + " rejected. Need work email address.");
            return Response.status(Status.BAD_REQUEST)
               .entity("Sorry, we can't sign you up with an email address " + domain + ". Try with your work email.")
               .build();
         }
         if (requestDao.searchByEmail(userMail) == null)
         {
            Response resp = super.createTenantWithEmailConfirmation(tName, userMail);
            new ReferencesManager(adminConfiguration).putEmail(userMail, (String)resp.getEntity());
         }
         else
         {
            LOG.info("User " + userMail + " already signed up to " + tName + ". Wait until a workspace will be created.");
            return Response
               .ok(
                  "You already signed up. Wait until your workspace will be created. We will inform you when it will be ready.")
               .build();
         }
      }
      catch (TenantAlreadyExistException ex)
      {
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.repository.name", tName);
         props.put("user.mail", userMail);

         try
         {
            TenantState tState = cloudInfoHolder.getTenantStatus(tName).getState();
            switch (tState) {
            case CREATION: case WAITING_CREATION:
            {
               props.put("rfid",
                  new ReferencesManager(adminConfiguration).putEmail(userMail, UUID.randomUUID().toString()));
               utils.sendOkToJoinEmail(userMail, props);
               return Response.ok().build();
            }
            case ONLINE:
            {
               if (utils.isNewUserAllowed(tName, username))
               {
                  // send OK email
                  props.put("rfid",
                     new ReferencesManager(adminConfiguration).putEmail(userMail, UUID.randomUUID().toString()));
                  utils.sendOkToJoinEmail(userMail, props);
                  return Response.ok().build();
               }
               else
               {
                  LOG.info("User " + userMail + " was put in waiting state - users limit reached.");
                  UserRequest req =
                     new UserRequest("", tName, userMail, "", "", "", "", "", "", false, RequestState.WAITING_LIMIT);
                  requestDao.put(req);
                  // send not allowed mails
                  props.put("users.maxallowed", Integer.toString(utils.getMaxUsersForTenant(tName)));
                  utils.sendJoinRejectedEmails(tName, userMail, props);
                  return Response.ok().build();
               }
            }
            case SUSPENDED:
            {
               LOG.info("User " + userMail + " was put in waiting state after singup - tenant suspended.");
               utils.resumeTenant(tName);
               UserRequest req =
                  new UserRequest("", tName, userMail, "", "", "", "", "", "", false, RequestState.WAITING_JOIN);
               requestDao.put(req);
               TenantCreatedListenerThread thread =
                        new TenantCreatedListenerThread(tName, cloudInfoHolder, adminConfiguration, requestDao);
                     ExecutorService executor = Executors.newSingleThreadExecutor();
                     executor.execute(thread);
               return Response.status(309)
                        .header("Location", "http://" + adminConfiguration.getMasterHost() + "/resuming.jsp?email=" + userMail)
                        .build();
            }
            default:
            {
               String msg =
                  "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
               LOG.warn("Signup failed for user " + userMail + ", tenant " + tName + " state is "
                  + cloudInfoHolder.getTenantStatus(tName).getState().toString());
               return Response.status(Status.BAD_REQUEST).entity(msg).build();
            }
           }
         }
         catch (UserAlreadyExistsException e)
         {
            // Custom status for disable ajax auto redirection;
            LOG.info("User " + userMail + " already signed up to " + tName + ". Redirect to signin page.");
            return Response.status(309)
               .header("Location", "http://" + adminConfiguration.getMasterHost() + "/signin.jsp?email=" + userMail)
               .build();
         }
      }
      return Response.ok().build();
   }
   
   /**
    * Sign-up the Cloud. Result is a registration URL for a registeration or to join to an existing tenant.<br/> 
    * This URL can be used to proceed the user registration on to the Cloud.<br/> 
    * 
    * <p>Specification</p>
    * <ul> 
    * <li>Service return status 201 Created if such tenant can be created in the cloud, the response entity will contain an URL for a registration of a new tenant. This registration URL will be actual during some fixed period in time (6h currently).</li>
    * <li>If requested tenant already exists and this user can be joined, the service will return status 200 OK and a link to join to the tenant.</li>
    * <li>If such tenant already exists and an user already signed up to the tenant (or it is in progress), the service will return client error 409 Conflict and a message "User EMAIL already signed up to TENANT_NAME.".</li>
    * <li>In case if a tenant creation isn't possible a related message will be returned with status 400 Bad Request.</li> 
    * <li>For an error, teh error message will be returned with status 500 Internal Server Error.</li>
    * </ul>
    * 
    * @param String userMail email address of user to signup/join.
    * @return Response with URL for a registration/join or with a client error.
    * @throws CloudAdminException if error occurs
    */
   @POST
   @Path("/signup-link")
   public Response signupLink(@FormParam("user-mail") String userMail) throws CloudAdminException
   {
      LOG.info("Received Signup Link request for " + userMail);
      String tName = null;
      String username = null;
      try
      {
         if (!utils.validateEmail(userMail)) 
         {
            return Response.status(Status.BAD_REQUEST).entity("Invalid email address.").build();
         }

         username = userMail.substring(0, (userMail.indexOf("@")));
         tName = utils.getTenantNameFromWhitelist(userMail);

         if (tName == null)
         {
            String domain = userMail.substring(userMail.indexOf("@"));
            return Response.status(Status.BAD_REQUEST)
               .entity("Cannot sign up with an email address " + domain + ". Require work email.").build();
         }
         if (requestDao.searchByEmail(userMail) == null)
         {
            String uuid = super.createTenant(tName, userMail);
            new ReferencesManager(adminConfiguration).putEmail(userMail, uuid);
            URI location = URI.create("http://" + adminConfiguration.getMasterHost() + "/registration.jsp?id=" + uuid); 
            return Response.created(location).entity(location.toString()).build();
         }
         else
         {
            LOG.info("Client error: user " + userMail + " already signed up to " + tName + ". Wait until a workspace will be created.");
            return Response.status(Status.CONFLICT).entity("User " + userMail + " already signed up to " + tName + 
                   ". Wait until a workspace will be created. The user will be informed when it will be ready.").build();
         }
      }
      catch (TenantAlreadyExistException ex)
      {
         try
         {
            TenantState tState = cloudInfoHolder.getTenantStatus(tName).getState();
            switch (tState) {
              case CREATION: case WAITING_CREATION: case SUSPENDED:
              {
                 final String uuid = UUID.randomUUID().toString();
                 new ReferencesManager(adminConfiguration).putEmail(userMail, uuid);
                 return Response.ok().entity("http://" + adminConfiguration.getMasterHost() + "/join.jsp?rfid=" + uuid).build();
              }
              case ONLINE:
              {
                 if (utils.isNewUserAllowed(tName, username))
                 {
                    // send OK email
                    final String uuid = UUID.randomUUID().toString();
                    new ReferencesManager(adminConfiguration).putEmail(userMail, uuid);
                    return Response.ok().entity("http://" + adminConfiguration.getMasterHost() + "/join.jsp?rfid=" + uuid).build();
                 }
                 else
                 {
                    LOG.info("Link request for join of user " + userMail +	" to " + tName + " rejected - users limit reached.");
                    return Response.status(Status.BAD_REQUEST)
                        .entity("Cannot invite " + userMail + " to " + tName + ". Maximum number of users reached.").build();
                 }
              }
              default:
              {
                 String msg =
                    "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
                 LOG.warn("Link request for signup of user " + userMail + " failed, tenant " + tName + " state is "
                    + cloudInfoHolder.getTenantStatus(tName).getState().toString());
                 return Response.status(Status.BAD_REQUEST).entity(msg).build();
              }
            }
         }
         catch (UserAlreadyExistsException e)
         {
            LOG.info("Client error: user " + userMail + " already signed up to " + tName + ".");
            return Response.status(Status.CONFLICT).entity("User " + userMail + " already signed up to " + tName + ".").build();
         }
      }
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
      @FormParam("confirmation-id") String uuid, @FormParam("rfid") String hash) throws CloudAdminException
   {
      String tName = null;
      String username = null;
      try
      {
         if (!utils.validateEmail(userMail))
            return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();

         if (!utils.validateUUID(userMail, hash))
            return Response.status(Status.BAD_REQUEST).entity("Email address provided does not match with hash.")
               .build();
         else
            new ReferencesManager(adminConfiguration).removeEmail(userMail);

         username = userMail.substring(0, (userMail.indexOf("@")));
         String tail = userMail.substring(userMail.indexOf("@") + 1);
         tName = tail.substring(0, tail.indexOf(".")).toLowerCase();
         // Prepare properties for mailing
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.repository.name", tName);
         props.put("user.mail", userMail);
         props.put("user.name", username);
         props.put("first.name", firstName);
         props.put("last.name", lastName);

         // Storing user & sending appropriate mails
         TenantState tState = cloudInfoHolder.getTenantStatus(tName).getState();
         switch (tState)
         {
            case ONLINE : 
            {

               try
               {
                  if (utils.isNewUserAllowed(tName, username))
                  {
                     utils.storeUser(tName, userMail, firstName, lastName, password, false);
                     utils.sendUserJoinedEmails(tName, firstName, userMail, props);
                  }
                  else
                  {
                     // Limit reached
                     LOG.info("User " + userMail + " join was put in waiting state - users limit reached.");
                     UserRequest req =
                        new UserRequest("", tName, userMail, firstName, lastName, "", "", password, "", false,
                           RequestState.WAITING_LIMIT);
                     requestDao.put(req);
                     props.put("users.maxallowed", Integer.toString(utils.getMaxUsersForTenant(tName)));
                     utils.sendJoinRejectedEmails(tName, userMail, props);
                     return Response.ok().build();
                  }

               }
               catch (UserAlreadyExistsException e)
               {
                  LOG.warn("User " + username + " already registered on workspace " + tName
                     + ". Join request rejected. User warned on the Sign Up form.");
                  return Response.ok(e.getMessage()).build();
               }

            }
            case CREATION : 
            case WAITING_CREATION : 
            {
               UserRequest req =
                  new UserRequest("", tName, userMail, firstName, lastName, "", "", password, "", false,
                     RequestState.WAITING_JOIN);
               requestDao.put(req);
               return Response.ok().build();
            }
            case SUSPENDED : 
            {
               utils.resumeTenant(tName);
               LOG.info("User " + userMail + " was put in waiting state after join - tenant suspended.");
               UserRequest req =
                  new UserRequest("", tName, userMail, firstName, lastName, "", "", password, "", false,
                     RequestState.WAITING_JOIN);
               requestDao.put(req);
               TenantCreatedListenerThread thread =
                  new TenantCreatedListenerThread(tName, cloudInfoHolder, adminConfiguration, requestDao);
               ExecutorService executor = Executors.newSingleThreadExecutor();
               executor.execute(thread);
               return Response
                  .status(309)
                  .header("Location",
                     "http://" + adminConfiguration.getMasterHost() + "/resuming.jsp?email=" + userMail).build();
            }
            default : 
            {
               String msg =
                  "Sorry, we cannot process your join request right now, workspace seems not ready. Please, try again later.";
               LOG.warn("Joining user " + userMail + " failed, tenant " + tName + " state is "
                  + cloudInfoHolder.getTenantStatus(tName).getState().toString());
               return Response.status(Status.BAD_REQUEST).entity(msg).build();
            }
         }

      }
      catch (CloudAdminException e)
      {
         LOG.warn("User " + username + " join failed, put him in join queue.");
         UserRequest req =
            new UserRequest("", tName, userMail, firstName, lastName, "", "", password, "", false,
               RequestState.WAITING_JOIN);
         requestDao.put(req);
      }
      return Response.ok().build();
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
   protected Response createIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
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
         return Response.serverError().entity((String)resp.getEntity()).build();
      TenantCreatedListenerThread thread =
         new TenantCreatedListenerThread(tName, cloudInfoHolder, adminConfiguration, requestDao);
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
   @Produces(MediaType.TEXT_PLAIN)
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

      if (utils.validateUUID(userMail, uuid))
         new ReferencesManager(adminConfiguration).removeEmail(userMail);
      //return Response.status(Status.BAD_REQUEST).entity("Email address provided does not match with hash.").build();
      //else
      //new ReferencesManager(adminConfiguration).removeEmail(userMail);

      String tName = utils.getTenantNameFromWhitelist(userMail);
      if (tName == null)
      {
         String domain = userMail.substring(userMail.indexOf("@"));
         return Response.status(Status.BAD_REQUEST)
            .entity("Sorry, we can't create workspace with an email address " + domain + ". Try with your work email.")
            .build();
      }
      UserRequest req =
         new UserRequest("", tName, userMail, firstName, lastName, companyName, phone, password, uuid, true,
            RequestState.WAITING_CREATION);
      requestDao.put(req);
      Map<String, String> props = new HashMap<String, String>();
      String username = userMail.substring(0, (userMail.indexOf("@")));
      props.put("tenant.masterhost", adminConfiguration.getMasterHost());
      props.put("tenant.repository.name", tName);
      props.put("user.mail", userMail);
      props.put("user.name", username);
      props.put("first.name", firstName);
      props.put("last.name", lastName);
      utils.sendCreationQueuedEmails(tName, userMail, props);
      return Response.ok().build();
   }

   @GET
   @RolesAllowed("cloud-manager")
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/requests")
   public Map<String, String[]> getTenantRequests() throws CloudAdminException
   {
      Map<String, String[]> result = new HashMap<String, String[]>();
      List<UserRequest> list = requestDao.search(null, RequestState.WAITING_CREATION);
      if (list.isEmpty())
         return result;

      for (UserRequest one : list)
      {
         try
         {
            String tName = one.getTenantName();
            String[] data = new String[5];
            data[0] = tName;
            data[1] = one.getUserEmail();
            data[2] = one.getFirstName() + " " + one.getLastName();
            data[3] = one.getCompanyName();
            data[4] = one.getPhone();
            result.put(one.getFileName().substring(0, one.getFileName().indexOf(".")), data);
         }
         catch (Exception e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail(e.getMessage(), e);
            throw new CloudAdminException(
               "A problem happened during retrieving requests list . It was reported to developers. Please, try again later.");
         }
      }
      return utils.sortByComparator(result);
   }

   @GET
   @Path("/validate/{decision}/{filename}")
   @RolesAllowed("cloud-manager")
   @Produces(MediaType.TEXT_PLAIN)
   public Response validate(@PathParam("decision") String decision, @PathParam("filename") String filename)
      throws CloudAdminException
   {
      filename = filename + ".properties";
      UserRequest req = requestDao.searchByFilename(filename);
      if (req == null)
      {
         LOG.warn("Validation requested file which can not be found anymore: " + filename);
         return Response.serverError().entity("File can not be found on server anymore.").build();
      }

      if (decision.equalsIgnoreCase("accept"))
      {
         Response resp =
            createIntranet(req.getUserEmail(), req.getFirstName(), req.getLastName(), req.getCompanyName(),
               req.getPhone(), req.getPassword(), req.getConfirmationId());

         if (resp.getStatus() == 200 && resp.getEntity() == null)
         {
            List<UserRequest> list = requestDao.search(req.getTenantName(), RequestState.WAITING_CREATION);
            for (UserRequest one : list)
            {
               UserRequest req_new =
                  new UserRequest(one.getFileName(), one.getTenantName(), one.getUserEmail(), one.getFirstName(),
                     one.getLastName(), one.getCompanyName(), one.getPhone(), one.getPassword(),
                     one.getConfirmationId(), one.getUserEmail().equals(req.getUserEmail()) ? true : false,
                     RequestState.WAITING_JOIN);
               requestDao.delete(one);
               try
               {
                  Thread.sleep(100); //To let FS finish
               }
               catch (InterruptedException e)
               {
                  LOG.warn(e.getMessage());
               }
               requestDao.put(req_new);
            }
            return resp;
         }
         else
         {
            String msg = "Can not finish accept operation - service returned HTTP status " + resp.getStatus();
            LOG.error(msg);
            utils.sendAdminErrorEmail(msg, null);
            return Response.serverError().entity("Operation failed. It was reported to developers.").build();
         }
      }
      else if (decision.equalsIgnoreCase("refuse"))
      {
         LOG.info("Tenant " + req.getTenantName() + " creation was refused.");
         requestDao.delete(req);
         return Response.ok().build();
      }
      else if (decision.equalsIgnoreCase("blacklist"))
      {
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("user.name", req.getFirstName());
         utils.sendCreationRejectedEmail(req.getTenantName(), req.getUserEmail(), props);
         utils.putInBlackList(req.getUserEmail());
         requestDao.delete(req);
         return Response.ok().build();
      }
      else
      {
         throw new CloudAdminException("Unknown action.");
      }
   }

   @GET
   @Path("autojoin/{state}")
   public Response autojoin(@PathParam("state") String state) throws CloudAdminException
   {
      utils.joinAll(null, RequestState.valueOf(state));
      return Response.ok().build();
   }

   //   @GET
   //   @Path("/isuserallowed/{tenantname}/{username}")
   //   @Produces(MediaType.TEXT_PLAIN)
   //   public Response isuserallowed(@PathParam("tenantname") String tName, @PathParam("username") String username)
   //      throws CloudAdminException{
   //      if (utils.isNewUserAllowed(tName, username))
   //         return Response.ok("TRUE").build();
   //      else
   //         return Response.ok("FALSE").build();
   //      
   //   }
   
   
      @GET
      @Path("/isuserexist/{tenantname}/{username}")
      @Produces(MediaType.TEXT_PLAIN)
      public Response isuserexist(@PathParam("tenantname") String tName, @PathParam("username") String username)
         throws CloudAdminException{
          try 
          {
             utils.isNewUserAllowed(tName, username);
             return Response.ok("FALSE").build();
          }
          catch (UserAlreadyExistsException e){
             return Response.ok("TRUE").build();
          }
      }

   

   @GET
   @Path("/maxallowed/{tenantname}")
   @Produces(MediaType.TEXT_PLAIN)
   public Response maxallowed(@PathParam("tenantname") String tName) throws CloudAdminException
   {
      return Response.ok(Integer.toString(utils.getMaxUsersForTenant(tName))).build();
   }

   @GET
   @Path("uuid/{uuid}")
   @Produces(MediaType.TEXT_PLAIN)
   public Response uuid(@PathParam("uuid") String uuid) throws CloudAdminException
   {
      String email = new ReferencesManager(adminConfiguration).getEmail(uuid);
      if (email != null)
         return Response.ok(email).build();
      else
         return Response.status(Status.BAD_REQUEST)
            .entity("Warning! You are using broken link to the Registration Page. Please sign up again.").build();
   }
   
   
   @GET
   @Path("passrestore/{email}")
   @Produces(MediaType.TEXT_PLAIN)
   public Response passrestore(@PathParam("email") String email) throws CloudAdminException
   {
      if (!utils.validateEmail(email))
         return Response.status(Status.BAD_REQUEST).entity("Please enter a valid email address.").build();

      ChangePasswordManager manager = new ChangePasswordManager(adminConfiguration);
      String username = email.substring(0, (email.indexOf("@")));
      String tail = email.substring(email.indexOf("@") + 1);
      String tName = tail.substring(0, tail.indexOf(".")).toLowerCase();

      TenantState tState;
      try
      {
         tState = cloudInfoHolder.getTenantStatus(tName).getState();
      }
      catch (TenantQueueException e)
      {
         return Response.status(Status.BAD_REQUEST)
            .entity("This email " + email + " is not registered on Cloud Workspaces.").build();
      }
      switch (tState)
      {
         case ONLINE : {

            if (isuserexist(tName, username).getEntity().equals("TRUE"))
            {
               String uuid = manager.addReference(email);
               utils.sendPasswordRestoreEmail(email, tName, uuid);
            }
            else
            {
               return Response.status(Status.BAD_REQUEST)
                  .entity("User with email " + email + " is not registered on Cloud Workspaces.").build();
            }
            return Response.ok().build();
         }

         case SUSPENDED : {
            return Response.status(309)
               .header("Location", "http://" + adminConfiguration.getMasterHost() + "/resuming.jsp?email=" + email)
               .build();
         }
         
         default: {
            return Response.status(Status.BAD_REQUEST)
                     .entity("Workspace " + tName + " seems not ready. Please, try again later.").build();
         }
      }
   }

   @POST
   @Path("passconfirm")
   @Produces(MediaType.TEXT_PLAIN)
   public Response passconfirm(@FormParam("uuid") String uuid, @FormParam("password") String password) throws CloudAdminException
   {
      ChangePasswordManager manager = new ChangePasswordManager(adminConfiguration);
      try 
      {
         String email =  manager.validateReference(uuid);
         utils.updatePassword(email, password);
         return Response.ok().build();
      } catch (CloudAdminException e)
      {
         return Response.serverError().entity(e.getMessage()).build();
      }
   }
   
   /**
    * Answers on question "Does the given email's domain address is blackisted?". 
    * Used in Invitation gadget.
    * 
    * @param String email
    * @return String, TRUE or FALSE - the answer on the question "Does the given email's domain address is blackisted?"
    */
   @GET
   @Path("blacklisted/{email}")
   @Produces(MediaType.TEXT_PLAIN)
   public Response balcklisted(@PathParam("email") String email)
   {
      if (utils.isInBlackList(email))
         return Response.ok("TRUE").build();
      else
         return Response.ok("FALSE").build();
   }
   
}
