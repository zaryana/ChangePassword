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
package com.exoplatform.cloudworkspaces;

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.exoplatform.cloudmanagement.admin.AgentAuthenticator;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.MailSender;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ConfigurationParameterNotFound;
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
   
   private static final String CLOUD_ADMIN_TENANT_MAXUSERS = "cloud.admin.tenant.maxusers";

   private CloudAdminConfiguration cloudAdminConfiguration;

   private MailSender mailSender;
   
   private String whiteListConfigurationFile;

   private static final Logger LOG = LoggerFactory.getLogger(CloudIntranetUtils.class);

   public CloudIntranetUtils(CloudAdminConfiguration cloudAdminConfiguration)
   {
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.mailSender = new MailSender(cloudAdminConfiguration);
      this.whiteListConfigurationFile = System.getProperty("cloud.admin.whitelist");
      
      Authenticator.setDefault(new AgentAuthenticator(cloudAdminConfiguration.getProperty("admin.agent.auth.username",
         null), cloudAdminConfiguration.getProperty("admin.agent.auth.password", null)));
   }

   public void storeUser(String tName,  String userMail, String firstName, String lastName, String password)
      throws CloudAdminException
   {
      String username = userMail.substring(0, (userMail.indexOf("@")));
      
      URL url;
      HttpURLConnection connection = null;
      
      StringBuilder hostName = new StringBuilder();
      hostName.append(tName);
      hostName.append(".");
      hostName.append(cloudAdminConfiguration.getProperty("cloud.admin.frontend.server.host"));
      
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(hostName);
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
            String err = readText(connection.getErrorStream());
            LOG.error("Unable to add user to tenant " + tName + " (" + hostName + ") - HTTP status:"
                    + connection.getResponseCode() 
                    + (err != null ? ". Server error: \r\n" + err  + "\r\n": ""));
            throw new CloudAdminException("Internal error occured while adding user. Please contact support");
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
      strUrl.append(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_FRONT_END_SERVER_HOST));
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
            String err = readText(connection.getErrorStream());
            LOG.error("Unable to add root user to tenant " + tName + " - HTTP status:"
                    + connection.getResponseCode() 
                    + (err != null ? ". Server error: \r\n" + err  + "\r\n" : ""));
            throw new CloudAdminException("Internal error occured while adding onwer. Please contact support");
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

   public boolean isNewUserAllowed(String _tName, String _username, int maxUsers) throws CloudAdminException
   {
      if (_tName == null || _username == null)
    	  throw new CloudAdminException("Cannot validate user " + _username + " on tenant " + _tName);
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
                        {
                           counter++;
                        }
                        else
                        {
                           throw new UserAlreadyExistsException("This user has already registered on tenant " + tenantName);
                        }
                     }
                  }
               }
            }
            if (maxUsers == -1 || counter <= maxUsers) 
            {
               return true;
            }
            else
            {
               return false;
            }
         }
         else
         {
            String err = readText(connection.getErrorStream());
            LOG.error("Unable to get user list from tenant " + _tName + " - HTTP status"
                    + connection.getResponseCode() 
                    + (err != null ? ". Server error: \r\n" + err  + "\r\n" : ""));
            throw new CloudAdminException("Internal error occured on getting users list. Please contact support");
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
            String email = "";

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
 
                        if (userName.equalsIgnoreCase("root")) 
                        {
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
            String err = readText(connection.getErrorStream());
            LOG.error("Unable to get owner from tenant " + _tName + " - HTTP status"
                    + connection.getResponseCode() 
                    + (err != null ? ". Server error: \r\n" + err  + "\r\n" : ""));
            throw new CloudAdminException("Can not find tenant owner. Please, contact support.");
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
         LOG.error("Confuiguration error", e);
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
         mailSender.sendMail(props.get("owner.email"), cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT).
        		 replace("${company}", props.get("tenant.repository.name")),ownerTemplate, props);
         mailSender.sendMail(salesEmail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT).
        		 replace("${company}", props.get("tenant.repository.name")), salesTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error("Configuration error", e);
      }
   }

   public void sendUserJoinedEmails(String tName, String firstName,  String userMail, Map<String, String> props) throws CloudAdminException
   {
      String userTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE, null);
      String ownerSubject = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT).
     		 replace("${company}", tName);
      ownerSubject = ownerSubject.replace("${firstname}", firstName);
      try
      {
         String ownerEmail = getTenantOwnerEmail(tName);
         if (ownerEmail == null)
            throw new CloudAdminException("Cannot get owner email for tenant " + tName);
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT).
        		 replace("${company}", tName), userTemplate, props);
         mailSender.sendMail(ownerEmail,ownerSubject, ownerTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
        LOG.error("Configuration error", e);
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
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT).
            replace("${company}", props.get("tenant.repository.name")), userTemplate, props);
         mailSender.sendMail(userMail,
            cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_OWNER_INTRANET_CREATED_SUBJECT).
            replace("${company}", props.get("tenant.repository.name")), ownerTemplate, props);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error("Configuration error", e);
      }
   }
   
   
   public void sendAdminErrorEmail(String msg, Exception error)
   {
      String mailTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE, null);
      String mailSubject =
         cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT, "Cloud admin error");

      Map<String, String> props = new HashMap<String, String>();
      props.put("message", msg);
      
      String prettyMsg = error.getMessage().replaceAll("(\r\n|\n\r|\r|\n)", "<br>"); 
      prettyMsg = prettyMsg.replaceAll("(\t)", "&nbsp;&nbsp;&nbsp;&nbsp;");
      props.put("exception.message", prettyMsg);
      
      StringBuilder trace = new StringBuilder();
      for (StackTraceElement item: error.getStackTrace())
      {
         String line = item.toString();
         if (line.startsWith("at ")) {
           trace.append("&nbsp;&nbsp;&nbsp;&nbsp;");
         } 
         trace.append(item.toString());
         trace.append("<br>");
      }
      props.put("stack.trace", trace.toString());
      
      try
      {
         for (String email : cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_EMAIL).split(","))
         {
            mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props);
         }
      }
      catch (CloudAdminException ex)
      {
         LOG.error("Cannot send mail message to admin. Message was '" + msg + "' it is caused by '" + error.getMessage() + "'.", ex);
      }
   }
   
   public String getTenantNameFromWhitelist(String email) throws CloudAdminException
   {
      String tail = email.substring(email.indexOf("@") + 1);
      if (whiteListConfigurationFile == null)
      {
         LOG.warn("White list configuration property not found, registration disabled!");
         return null;
      }
      String value = null;
      String tName = null;
      File propertyFile = new File(whiteListConfigurationFile);
      try
      {
         FileInputStream io = new FileInputStream(propertyFile);
         Properties properties = new Properties();
         properties.load(io);
         value = properties.getProperty(tail);
         if (value == null)
            return null;
         if (value.indexOf(":") > -1)
         {
            tName = value.substring(0, value.indexOf(":"));
         }
         else
         {
            tName = value;
         }
         io.close();
      }
      catch (FileNotFoundException e)
      {
    	 LOG.error("White list configuration file not found. Registration disabled!");
         throw new CloudAdminException("Internal error occured. Please contact support.");
      }
      catch (IOException e)
      {
         LOG.error("White list configuration file read error. Registration disabled!");
         throw new CloudAdminException("Internal error occured. Please contact support.");
      }
      return tName;
   }
   
   public int getMaxUsersForTenant(String email) throws CloudAdminException
   {
      String tail = email.substring(email.indexOf("@") + 1);
      if (whiteListConfigurationFile == null)
      {
         LOG.warn("White list configuration property not found, user limits disabled!");
         return -1;
      }
      String value = null;
      int count = -1;
      File propertyFile = new File(whiteListConfigurationFile);
      try
      {
         FileInputStream io = new FileInputStream(propertyFile);
         Properties properties = new Properties();
         properties.load(io);
         value = properties.getProperty(tail);
         if (value == null)
            throw new CloudAdminException("This domain is not allowed to create tenants. Please contact support.");
         if (value.indexOf(":") > -1) 
         {
           count = Integer.parseInt(value.substring(value.indexOf(":")+1));
         } else {
           count = Integer.parseInt(cloudAdminConfiguration.getProperty("CLOUD_ADMIN_TENANT_MAXUSERS", "20")); 
         } 
         io.close();
      }
      catch (FileNotFoundException e)
      {
    	  LOG.error("White list configuration file not found. Registration disabled!");
          throw new CloudAdminException("Internal error occured. Please contact support.");
      }
      catch (IOException e)
      {
    	  LOG.error("White list configuration file read error. Registration disabled!");
          throw new CloudAdminException("Internal error occured. Please contact support.");
      }
      return count;
   } 
   
   
	public void validateEmail(String email) throws CloudAdminException {
	 //TODO: more serious check
     int index = email.indexOf("@");
     if (index == -1)
	   throw new CloudAdminException("Email is incorrect, please provide correct one.");

	}   
   
  /**
   * Read text message from InputStream. 
   * @param errStream InputStream
   * @return String
   * @throws IOException 
   */
  protected String readText(InputStream errStream) throws IOException {
     if (errStream != null) 
     {
       InputStreamReader errReader = new InputStreamReader(errStream);
       try 
       {
         int r = -1;
         StringBuilder errText = new StringBuilder();
         char[] buff = new char[256]; 
         while ((r = errReader.read(buff)) >= 0) 
         {
           errText.append(buff, 0, r);
         }
         return errText.toString();
       } 
       finally 
       {
         errReader.close();
       }
     } 
     else 
     {
       return null;
     }
   }
}
