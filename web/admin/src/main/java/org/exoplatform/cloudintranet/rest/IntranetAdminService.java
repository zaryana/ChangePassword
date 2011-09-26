package org.exoplatform.cloudintranet.rest;

import static org.exoplatform.cloudmanagement.rest.admin.CloudAdminRestServicePaths.CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.cloudintranet.UserAlreadyExistsException;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.exoplatform.cloudmanagement.admin.rest.TenantCreator;
import org.exoplatform.cloudmanagement.admin.rest.CloudAdminExceptionMapper;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.MailSender;
import org.exoplatform.cloudmanagement.admin.TenantMetadataValidator;
import org.exoplatform.cloudmanagement.admin.TenantRegistrationException;
import org.exoplatform.cloudmanagement.admin.TenantAlreadyExistException;
import org.exoplatform.cloudmanagement.admin.TenantValidationException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.creation.TenantCreationSupervisor;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;

@Path(CLOUD_ADMIN_PUBLIC_TENANT_CREATION_SERVICE)
public class IntranetAdminService extends TenantCreator
{

   /* Cloud-intranet mail  */
   public final static String CLOUD_ADMIN_MAIL_JOIN_SUBJECT = "cloud.admin.mail.join.subject";

   public final static String CLOUD_ADMIN_MAIL_JOIN_TEMPLATE = "cloud.admin.mail.join.template";

   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT = "cloud.admin.mail.join.closed.user.subject";

   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE = "cloud.admin.mail.join.closed.user.template";

   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT = "cloud.admin.mail.join.closed.owner.subject";

   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE =
      "cloud.admin.mail.join.closed.owner.template";

   public final static String CLOUD_ADMIN_MAIL_SALES_EMAIL = "cloud.admin.mail.sales.email";
   
   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT = "cloud.admin.mail.join.closed.sales.subject";

   public final static String CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE =
      "cloud.admin.mail.join.closes.sales.template";

   public IntranetAdminService(CloudInfoHolder cloudInfoHolder,
      TenantMetadataValidator tenantMetadataValidator, CloudAdminConfiguration cloudAdminConfiguration,
      TenantCreationSupervisor creationSupervisor)
   {
      super(cloudInfoHolder, tenantMetadataValidator, cloudAdminConfiguration, creationSupervisor);
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
      try
      {
         super.createTenantWithEmailConfirmation(tName, userMail);
      }
      catch (TenantAlreadyExistException ex)
      {
         MailSender mailSender = new MailSender(adminConfiguration);

         try
         {
            if (isNewUserAllowed(tName, username))
            {
               //send OK email 
               Map<String, String> props = new HashMap<String, String>();
               props.put("tenant.masterhost", adminConfiguration.getMasterHost());
               props.put("tenant.name", tName);
               props.put("user.mail", userMail);
               String mailTemplate = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_TEMPLATE, null);

               mailSender.sendMail(userMail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_SUBJECT),
                  mailTemplate, props);

            }
            else
            {
               //send not allowed mails
               Map<String, String> props = new HashMap<String, String>();
               props.put("tenant.masterhost", adminConfiguration.getMasterHost());
               props.put("tenant.name", tName);
               props.put("user.mail", userMail);

               String userTemplate = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE, null);
               String ownerTemplate = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE, null);
               String salesEmail = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SALES_EMAIL, null);
               String salesTemplate = adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE, null);

               mailSender.sendMail(userMail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT),
                  userTemplate, props);
               //					mailSender.sendMail(ownerMail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT),
               //							ownerTemplate, props);
               //					mailSender.sendMail(salesEmail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT),
               //							salesTemplate, props);

            }
         }
         catch (CloudAdminException e)
         {
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
      return storeUser(userMail, firstName, lastName, password);

   }

   @POST
   @Path("/create")
   public Response createIntranet(@FormParam("user-mail") String userMail, @FormParam("first-name") String firstName,
      @FormParam("last-name") String lastName, @FormParam("company-name") String companyName,
      @FormParam("phone") String phone, @FormParam("password") String password,
      @FormParam("confirmation-id") String uuid) throws CloudAdminException
   {
      super.createTenantWithConfirmedEmail(uuid);
      return Response.ok().build();
   }

   private Response storeUser(String userMail, String firstName, String lastName, String password)
      throws CloudAdminException
   {

      String tName = userMail.substring(userMail.indexOf("@") + 1, userMail.indexOf(".", userMail.indexOf("@")));
      String username = userMail.substring(0, (userMail.indexOf("@")));

      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(adminConfiguration.getMasterHost());
      strUrl.append("/rest/organization/adduser");

      StringBuilder params = new StringBuilder();
      params.append("URI=" + "/" + username);
      params.append("&");
      params.append("username=" + username);
      params.append("&");
      params.append("password=" + password);
      params.append("&");
      params.append("first-name=" + firstName);
      params.append("&");
      params.append("last-name=" + lastName);
      params.append("&");
      params.append("email=" + userMail);
      try
      {
         url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("POST");
         connection.setDoOutput(true);
         OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
         writer.write(params.toString());
         writer.flush();
         writer.close();
         if (connection.getResponseCode() == 201)
         {
            return Response.ok().build();
         }
         else
         {
            //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
            throw new CloudAdminException("Unable to add user to tenant " + tName + " - HTTP status:"
               + connection.getResponseCode());
         }

      }
      catch (MalformedURLException e)
      {
         //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (IOException e)
      {
         //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      finally
      {

         if (connection != null)
         {
            connection.disconnect();
         }
      }

   }

   private boolean isNewUserAllowed(String _tName, String _username) throws CloudAdminException
   {
      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(adminConfiguration.getMasterHost());
      strUrl.append("/rest/cloud-admin/info-service/users-list");

      try
      {
         url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");

         InputStream io;
         //read Response
         if (connection.getResponseCode() < 400)
         {
            io = connection.getInputStream();
         }
         else
         {
            io = connection.getErrorStream();
         }

         if (connection.getResponseCode() == 200)
         {
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(io);
            ObjectValue responseObj = (ObjectValue)jsonParser.getJsonObject();
            int counter = 0;

            Iterator<String> as = responseObj.getKeys();
            while (as.hasNext())
            {
               String asName = as.next();
               Iterator<String> tenant = responseObj.getElement(asName).getKeys();
               while (tenant.hasNext())
               {
                  String tenantName = tenant.next();
                  if (tenantName.equalsIgnoreCase(_tName))
                  {
                     Iterator<String> users = responseObj.getElement(asName).getElement(tenantName).getKeys();
                     while (users.hasNext())
                     {
                        String userName = users.next();
                        if (!userName.equalsIgnoreCase(_username))
                           counter++;
                        else
                           throw new UserAlreadyExistsException("This user already registered on tenant " + tenantName);
                     }
                  }
               }
            }
            if (counter < 20)
               return true;
            else
               return false;
         }
         else
         {
            //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
            throw new CloudAdminException("Unable to get user list from tenant " + _tName + " - HTTP status"
               + connection.getResponseCode());
         }
      }
      catch (MalformedURLException e)
      {
         //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (IOException e)
      {
         //LOG.error("Error during sending request of tenant creation to server " + server.getAlias(), e);
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (JsonException e)
      {
         //LOG.error("Error while parsing json from cloud-agent of " + server.getAlias(), e);
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }

   }
}
