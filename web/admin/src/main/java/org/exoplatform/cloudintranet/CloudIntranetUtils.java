package org.exoplatform.cloudintranet;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.MailSender;
import org.exoplatform.cloudmanagement.admin.AgentAuthenticator;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ConfigurationParameterNotFound;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.net.Authenticator;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudIntranetUtils
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

   private static final String CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE = "cloud.admin.mail.joined.template";

   private static final String CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT = "cloud.admin.mail.joined.subject";

   private static final String CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE =
      "cloud.admin.mail.user.joined.owner.template";

   private static final String CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT =
      "cloud.admin.mail.user.joined.owner.subject";

   private static final String CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE =
      "cloud.admin.mail.intranet.created.user.template";

   private static final String CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT =
      "cloud.admin.mail.intranet.created.user.subject";

   private static final String CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_TEMPLATE =
      "cloud.admin.mail.intranet.created.owner.template";

   private static final String CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_SUBJECT =
      "cloud.admin.mail.intranet.created.owner.subject";

   private CloudAdminConfiguration cloudAdminConfiguration;

   private MailSender mailSender;
   
   private static final Logger LOG = LoggerFactory.getLogger(CloudIntranetUtils.class);

   public CloudIntranetUtils(CloudAdminConfiguration cloudAdminConfiguration)
   {
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.mailSender = new MailSender(cloudAdminConfiguration);
      Authenticator.setDefault( new AgentAuthenticator(cloudAdminConfiguration.getProperty("admin.agent.auth.username", null), cloudAdminConfiguration.getProperty("admin.agent.auth.password", null)));
   }

   public void storeUser(String userMail, String firstName, String lastName, String password)
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
      strUrl.append(cloudAdminConfiguration.getMasterHost());
      strUrl.append("/cloud-agent/rest/organization/adduser");

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
         //connection.setRequestProperty("Authorization", "Basic " + encoding);
         connection.setRequestMethod("POST");
         connection.setDoOutput(true);
         OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
         writer.write(params.toString());
         writer.flush();
         writer.close();
         if (connection.getResponseCode() == 201)
         {
            return;
         }
         else
         {
            throw new CloudAdminException("Unable to add user to tenant " + tName + " - HTTP status:"
               + connection.getResponseCode());
         }

      }
      catch (MalformedURLException e)
      {
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (IOException e)
      {
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

   public void storeRoot(String tName, String userMail, String firstName, String lastName, String password)
      throws CloudAdminException
   {

      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(cloudAdminConfiguration.getMasterHost());
      strUrl.append("/cloud-agent/rest/organization/createroot");

      StringBuilder params = new StringBuilder();
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
         //connection.setRequestProperty("Authorization", "Basic " + encoding);
         connection.setRequestMethod("POST");
         connection.setDoOutput(true);
         
         OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
         writer.write(params.toString());
         writer.flush();
         writer.close();
         if (connection.getResponseCode() == 201)
         {
            return;
         }
         else
         {
            throw new CloudAdminException("Unable to add root user to tenant " + tName + " - HTTP status:"
               + connection.getResponseCode());
         }

      }
      catch (MalformedURLException e)
      {
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (IOException e)
      {
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

   public boolean isNewUserAllowed(String _tName, String _username) throws CloudAdminException
   {
      
      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(cloudAdminConfiguration.getMasterHost());
      strUrl.append("/cloud-admin/rest/private/cloud-admin/info-service/users-list");
      try
      {
         url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
        // connection.setRequestProperty("Authorization", "Basic " + encoding);
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
                        System.out.println("USER:" + userName);
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
            throw new CloudAdminException("Unable to get user list from tenant " + _tName + " - HTTP status"
               + connection.getResponseCode());
         }
      }
      catch (MalformedURLException e)
      {
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (IOException e)
      {
         throw new CloudAdminException(e.getLocalizedMessage(), e);
      }
      catch (JsonException e)
      {
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

   public void sendOkToJoinEmail(String userMail, Map<String, String> props) throws CloudAdminException
   {
      String mailTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_TEMPLATE, null);
      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_SUBJECT),
            mailTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
        LOG.error(e.getMessage());
      }

   }

   public void sendJoinRejectedEmails(String userMail, Map<String, String> props) throws CloudAdminException
   {
      String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE, null);
      String salesEmail = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_SALES_EMAIL, null);
      String salesTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE, null);

      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT),
            userTemplate, props);
         //             mailSender.sendMail(ownerMail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT),
         //                   ownerTemplate, props);
         //             mailSender.sendMail(salesEmail, adminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT),
         //                   salesTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }
   }

   public void sendUserJoinedEmails(String userMail, Map<String, String> props) throws CloudAdminException
   {

      String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE, null);
      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT),
            userTemplate, props);
         //       mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT),
         //       userTemplate, props);

      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }

   }

   public void sendIntranetCreatedEmails(String userMail, Map<String, String> props) throws CloudAdminException
   {

      //String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE, null);
      String ownerTemplate =
         cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_TEMPLATE, null);
      try
      {
//         mailSender.sendMail(userMail,
//            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT), userTemplate, props);
         mailSender.sendMail(userMail,
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_SUBJECT), ownerTemplate, props);

      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }
   }

}
