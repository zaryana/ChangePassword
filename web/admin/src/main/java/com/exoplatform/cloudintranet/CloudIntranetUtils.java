package com.exoplatform.cloudintranet;

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.MailSender;
import org.exoplatform.cloudmanagement.admin.AgentAuthenticator;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ConfigurationParameterNotFound;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.net.Authenticator;

import java.util.Map;
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
   
   private String whiteListConfigurationFile;

   private static final Logger LOG = LoggerFactory.getLogger(CloudIntranetUtils.class);

   public CloudIntranetUtils(CloudAdminConfiguration cloudAdminConfiguration)
   {
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.mailSender = new MailSender(cloudAdminConfiguration);
      this.whiteListConfigurationFile = System.getProperty("cloud.intranet.admin.whitelist");
      
      Authenticator.setDefault(new AgentAuthenticator(cloudAdminConfiguration.getProperty("admin.agent.auth.username",
         null), cloudAdminConfiguration.getProperty("admin.agent.auth.password", null)));
   }

   public void storeUser(String userMail, String firstName, String lastName, String password)
      throws CloudAdminException
   {

      String tName = emailToTenant(userMail);
      String username = userMail.substring(0, (userMail.indexOf("@")));

      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(cloudAdminConfiguration.getProperty("cloud.admin.frontend.server.host"));
      strUrl.append("/cloud-agent/rest/organization/adduser");

      StringBuilder params = new StringBuilder();
      params.append("tname=" + tName);
      params.append("&");
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
            return;
         }
         else
         {
            throw new CloudAdminException("Unable to add user to tenant " + tName + " - HTTP status:"
               + connection.getResponseCode()+ ". Please, contact support");
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
      strUrl.append(cloudAdminConfiguration.getProperty("cloud.admin.frontend.server.host"));
      strUrl.append("/cloud-agent/rest/organization/createroot");

      StringBuilder params = new StringBuilder();
      params.append("tname=" + tName);
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
            return;
         }
         else
         {
            throw new CloudAdminException("Unable to add root user to tenant " + tName + " - HTTP status:"
               + connection.getResponseCode() + ". Please, contact support");
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
      strUrl.append("/rest/private/cloud-admin/info-service/users-list");
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
                           throw new UserAlreadyExistsException("This user has already registered on tenant " + tenantName);
                     }
                  }
               }
            }
            if (counter < 20) //TODO: make configurable;
               return true;
            else
               return false;
         }
         else
         {
            throw new CloudAdminException("Unable to get user list from tenant " + _tName + " - HTTP status"
               + connection.getResponseCode() + ". Please, contact support");
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

   public String getTenantOwnerEmail(String _tName) throws CloudAdminException
   {

      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(cloudAdminConfiguration.getMasterHost());
      strUrl.append("/rest/private/cloud-admin/info-service/users-list");
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
            String email = null;

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
 
                        if (userName.equalsIgnoreCase("root")) {
                           email =
                              responseObj.getElement(asName).getElement(tenantName).getElement(userName)
                                 .getStringValue();
                           return email;
                        
                        }
                     }
                  }
               }
            }
            return email;
         }
         else
         {
            throw new CloudAdminException("Unable to get owner from tenant " + _tName + " - HTTP status"
               + connection.getResponseCode() + ". Please contact support.");
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
         mailSender.sendMail(props.get("owner.email"), cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT),
                            ownerTemplate, props);
         mailSender.sendMail(salesEmail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT),
                            salesTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }
   }

   public void sendUserJoinedEmails(String userMail, Map<String, String> props) throws CloudAdminException
   {

      String tName = emailToTenant(userMail);
      String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE, null);
      try
      {
         String ownerEmail = getTenantOwnerEmail(tName);
         if (ownerEmail == null)
            throw new CloudAdminException("Cannot get owner email for tenant " + tName);
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT),
            userTemplate, props);
         mailSender.sendMail(ownerEmail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT),
            ownerTemplate, props);

      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }

   }

   public void sendIntranetCreatedEmails(String userMail, Map<String, String> props) throws CloudAdminException
   {

      String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE, null);
      String ownerTemplate =
         cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_TEMPLATE, null);
      try
      {
         mailSender.sendMail(userMail,
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT), userTemplate, props);
         mailSender.sendMail(userMail,
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_SUBJECT), ownerTemplate, props);

      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage());
      }
   }
   
   
   public void sendAdminErrorEmail(String msg, Exception e)
   {

      String mailTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE, null);
      String mailSubject =
         cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT, "Cloud admin error");

      Map<String, String> props = new HashMap<String, String>();
      props.put("message", msg);
      props.put("exception.message", e.getMessage());

      String trace = null;

      for (int i = 0; i < e.getStackTrace().length; i++)
      {
         trace += e.getStackTrace()[i];
      }

      if (trace != null)
      {
         props.put("stack.trace", trace.replace("\n", "<br>"));
      }
      try
      {

         for (String email : cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_EMAIL).split(","))
         {
            mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props);
         }
      }
      catch (CloudAdminException ex)
      {
         LOG.error("Cannot send admin mail, " + e.getLocalizedMessage(), ex);
      }
   }
   
   public  String emailToTenant(String email) throws CloudAdminException {
      String tName = email.substring(email.indexOf("@") + 1, email.indexOf(".", email.indexOf("@")));
      if (tName == null || tName.length() == 0)
      {
         throw new CloudAdminException("E-mail validation failed. Please check the format of your email address.");
      }
      return tName;
   }
   
   
   
   public boolean checkWhiteList(String match) throws CloudAdminException
   {
      if (whiteListConfigurationFile == null)
      {
         LOG.warn("White list configuration property not found, registration disabled!");
         return false;
      }
      File propertyFile = new File(whiteListConfigurationFile);
      Scanner sc;
      try
      {
         sc = new Scanner(propertyFile);
         while (sc.hasNextLine())
         {
            String pattern = sc.nextLine();
            if (pattern.equalsIgnoreCase(match))
               return true;
         }
         return false;
      }
      catch (FileNotFoundException e)
      {
         throw new CloudAdminException("White list configuration file not found. Please contact support.");
      }
   }
   
   
}
