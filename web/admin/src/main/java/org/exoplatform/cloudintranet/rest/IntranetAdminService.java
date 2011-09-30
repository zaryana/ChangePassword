package org.exoplatform.cloudintranet.rest;

import static org.exoplatform.cloudmanagement.rest.admin.CloudAdminRestServicePaths.CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.cloudintranet.CloudIntranetUtils;
import org.exoplatform.cloudintranet.TenantCreatedListenerThread;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.admin.rest.CloudAdminExceptionMapper;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreatorWithCloudAdminRole;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantMetadataValidator;
import org.exoplatform.cloudmanagement.admin.TenantAlreadyExistException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.creation.TenantCreationSupervisor;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
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

   @POST
   @Path("/create-with-confirm/{tenantname}/{user-mail}")
   public Response createTenantWithEmailConfirmation(@PathParam("tenantname") String tenantName,
      @PathParam("user-mail") String userMail) throws CloudAdminException
   {
      return Response.status(Status.FORBIDDEN).entity("It's forbidden to use this method").build();
   }

   @POST
   @Path("/create-confirmed")
   public Response createTenantWithConfirmedEmail(@QueryParam("id") String uuid) throws CloudAdminException
   {
      return Response.status(Status.FORBIDDEN).entity("It's forbidden to use this method").build();
   }

   @POST
   @Path("/signup")
   public Response signupToIntranet(@FormParam("user-mail") String userMail) throws CloudAdminException
   {
      String tName = userMail.substring(userMail.indexOf("@") + 1, userMail.indexOf(".", userMail.indexOf("@")));
      String username = userMail.substring(0, (userMail.indexOf("@")));
      //TODO: white-list checking
      LOG.info("Received signup request from " + userMail);
      try
      {
         super.createTenantWithEmailConfirmation(tName, userMail);
      }
      catch (TenantAlreadyExistException ex)
      {
         try
         {
            if (utils.isNewUserAllowed(tName, username))
            {
               //send OK email 
               Map<String, String> props = new HashMap<String, String>();
               props.put("tenant.masterhost", adminConfiguration.getMasterHost());
               props.put("tenant.name", tName);
               props.put("user.mail", userMail);
              // utils.sendOkToJoinEmail(userMail, props);
            }
            else
            {
               //send not allowed mails
               Map<String, String> props = new HashMap<String, String>();
               props.put("tenant.masterhost", adminConfiguration.getMasterHost());
               props.put("tenant.name", tName);
               props.put("user.mail", userMail);
              // utils.sendJoinRejectedEmails(userMail, props);

            }
         }
         catch (CloudAdminException e)
         {
            e.printStackTrace();
            CloudAdminExceptionMapper mapper = new CloudAdminExceptionMapper();
            return mapper.toResponse(e);
         }
      }
      return Response.ok().build();
   }

   @POST
   @Path("/join")
   public Response joinIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      //TODO: control UUID
      try
      {
         utils.storeUser(userMail, firstName, lastName, password);
         Map<String, String> props = new HashMap<String, String>();
         props.put("tenant.masterhost", adminConfiguration.getMasterHost());
         props.put("tenant.name",
            userMail.substring(userMail.indexOf("@") + 1, userMail.indexOf(".", userMail.indexOf("@"))));
         props.put("user.mail", userMail);
        // utils.sendUserJoinedEmails(userMail, props);
         return Response.ok().build();
      }
      catch (CloudAdminException e)
      {
         e.printStackTrace();
         CloudAdminExceptionMapper mapper = new CloudAdminExceptionMapper();
         return mapper.toResponse(e);
      }

   }

   @POST
   @Path("/create")
   public Response createIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("company-name") String companyName,
      @FormParam("phone") String phone, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      super.createTenantWithConfirmedEmail(uuid);
      TenantCreatedListenerThread thread =
         new TenantCreatedListenerThread(userMail, firstName, lastName, companyName, phone, password, cloudInfoHolder,
            adminConfiguration);
      ExecutorService executor = Executors.newSingleThreadExecutor();
      try
      {
         Future<?> res = executor.submit(thread);
         res.get(30, TimeUnit.MINUTES);
      }
      catch (InterruptedException e)
      {
         LOG.error("Failed to create intranet " + e.getCause());
         e.printStackTrace();
      }
      catch (ExecutionException e)
      {
         LOG.error("Failed to create intranet " + e.getCause());
         e.printStackTrace();
      }
      catch (TimeoutException e)
      {
         LOG.error("Failed to create intranet " + e.getCause());
         e.printStackTrace();
      }
      return Response.ok().build();
   }

}
