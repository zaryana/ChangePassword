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
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_CREATED;

import com.exoplatform.cloudworkspaces.listener.TenantResumeThread;

import com.exoplatform.cloudworkspaces.listener.TenantCreatedListenerThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.impl.provider.json.JsonParser;
import org.everrest.core.impl.provider.json.ObjectValue;
import org.exoplatform.cloudmanagement.admin.AgentAuthenticator;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ConfigurationParameterNotFound;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudIntranetUtils
{
  
   private static final String CLOUD_ADMIN_TENANT_MAXUSERS = "cloud.admin.tenant.maxusers";

   private CloudAdminConfiguration cloudAdminConfiguration;
   
   private CloudInfoHolder holder;

   private WorkspacesMailSender mailSender;
   
   UserRequestDAO requestDao;
   
   private String whiteListConfigurationFile;
   
   private String blackListConfigurationFolder;
   
   private String maxUsersConfigurationFile;

   private static final Logger LOG = LoggerFactory.getLogger(CloudIntranetUtils.class);

   public CloudIntranetUtils(CloudAdminConfiguration cloudAdminConfiguration, CloudInfoHolder holder, UserRequestDAO requestDao)
   {
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.holder = holder;
      this.mailSender = new WorkspacesMailSender(cloudAdminConfiguration);
      this.whiteListConfigurationFile = System.getProperty("cloud.admin.whitelist");
      this.blackListConfigurationFolder = cloudAdminConfiguration.getProperty("cloud.admin.blacklist.dir", null);
      this.maxUsersConfigurationFile = System.getProperty("cloud.admin.userlimit");
      this.requestDao = requestDao;

      Authenticator.setDefault(new AgentAuthenticator(cloudAdminConfiguration.getProperty("admin.agent.auth.username",
         null), cloudAdminConfiguration.getProperty("admin.agent.auth.password", null)));
   }

   public void storeUser(String tName, String userMail, String firstName, String lastName, String password, boolean isAdministrator)
      throws CloudAdminException
   {
      String username = userMail.substring(0, (userMail.indexOf("@")));

      URL url;
      HttpURLConnection connection = null;

      StringBuilder hostName = new StringBuilder();
      hostName.append(tName);
      hostName.append(".");
      hostName.append(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_FRONT_END_SERVER_HOST));

      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(hostName);
      strUrl.append("/cloud-agent/rest/organization/adduser");

      StringBuilder params;
      try
      {
         params = new StringBuilder();
         params.append("tname=" + tName);
         params.append("&");
         params.append("URI=" + "/" + java.net.URLEncoder.encode(username, "utf-8"));
         params.append("&");
         params.append("username=" + java.net.URLEncoder.encode(username, "utf-8"));
         params.append("&");
         params.append("password=" + java.net.URLEncoder.encode(password, "utf-8"));
         params.append("&");
         params.append("first-name=" + java.net.URLEncoder.encode(firstName, "utf-8"));
         params.append("&");
         params.append("last-name=" + java.net.URLEncoder.encode(lastName, "utf-8"));
         params.append("&");
         params.append("email=" + java.net.URLEncoder.encode(userMail, "utf-8"));
         params.append("&");
         params.append("isadministrator=" + Boolean.toString(isAdministrator));
      }
      catch (UnsupportedEncodingException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException(
            "An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }

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
         if (connection.getResponseCode() == HTTP_CREATED)
         {
            return;
         }
         else
         {
            String err = readText(connection.getErrorStream());
            String msg = ("Unable to add user to workspace " + tName + " (" + hostName
               + ") - HTTP status:" + connection.getResponseCode()
               + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
            LOG.error(msg);
            sendAdminErrorEmail(msg, null);
            throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
         }
      }
      catch (MalformedURLException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");

      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }

   public boolean isNewUserAllowed(String tName, String username) throws CloudAdminException
   {
      if (tName == null || username == null)
         throw new CloudAdminException("Cannot validate user with such input data. Please, review it.");
      
      int maxUsers = getMaxUsersForTenant(tName);
      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_FRONT_END_SERVER_HOST));
      strUrl.append("/cloud-agent/rest/organization/users/"+ tName);
      strUrl.append("?");
      strUrl.append("administratorsonly=false");
      
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

         if (connection.getResponseCode() == HTTP_OK)
         {
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(io);
            ObjectValue responseObj = (ObjectValue)jsonParser.getJsonObject();
            int counter = 0;
            Iterator<String> users = responseObj.getKeys();
            while (users.hasNext())
            {
               String userName = users.next();
               String email = responseObj.getElement(userName).getStringValue();
               if (!userName.equalsIgnoreCase(username))
                {
                   if (!userName.equals("root"))
                     counter++;
                }
                else
                {
                   throw new UserAlreadyExistsException("This user has already registered on workspace " + tName);
                }
            }
            if (maxUsers == -1 || counter < maxUsers)
            {
               return true;
            }
            else
            {
               return false;
            }
         }
         else if (connection.getResponseCode() == 502)
         {
            throw new CloudAdminException("An problem happened during processsing this request: Workspace is not created yet or it was suspended.");
         }
         else
         {
            String err = readText(connection.getErrorStream());
            String msg = ("Unable to get user list from workspace " + tName + " - HTTP status:"
               + connection.getResponseCode() + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
            LOG.error(msg);
            sendAdminErrorEmail(msg, null);
            throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
         }
      }
      catch (MalformedURLException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      catch (JsonException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }
   
   public void updatePassword(String email, String password) throws CloudAdminException
   {
      if (email == null || password == null)
         throw new CloudAdminException("Cannot validate user with such input data. Please, review it.");

      String username = email.substring(0, (email.indexOf("@")));
      String tail = email.substring(email.indexOf("@") + 1);
      String tName = tail.substring(0, tail.indexOf("."));
      URL url;
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_FRONT_END_SERVER_HOST));
      strUrl.append("/cloud-agent/rest/organization/newpassword/");
      StringBuilder params;
      try
      {
         params = new StringBuilder();
         params.append("tname=" + tName);
         params.append("&");
         params.append("username="+ java.net.URLEncoder.encode(username, "utf-8"));
         params.append("&");
         params.append("password="+ java.net.URLEncoder.encode(password, "utf-8"));
      }
      catch (UnsupportedEncodingException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException(
            "An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
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
         if (connection.getResponseCode() == HTTP_OK)
         {
            return;
         }
         else
         {
            String err = readText(connection.getErrorStream());
            String msg =
               ("Unable to change password user "+ email +" to workspace " + tName +  " - HTTP status:"
                  + connection.getResponseCode() + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
            LOG.error(msg);
            sendAdminErrorEmail(msg, null);
            throw new CloudAdminException(
               "An problem happened during processsing this request. It was reported to developers. Please, try again later.");
         }
      }
      catch (MalformedURLException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException(
            "An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException(
            "An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }
   
   public Map<String,String> getTenantAdministrators(String tName) throws CloudAdminException
   {
   
      Map<String,String> result = new HashMap<String,String>();
      URL url;
      HttpURLConnection connection = null;

      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(tName);
      strUrl.append(".");
      strUrl.append(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_FRONT_END_SERVER_HOST));
      strUrl.append("/cloud-agent/rest/organization/users/"+ tName);
      strUrl.append("?");
      strUrl.append("administratorsonly=true");
      
      InputStream io;
      try
      {
         url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() == HTTP_OK)
         {
            io = connection.getInputStream();
            JsonParser jsonParser = new JsonParser();
            jsonParser.parse(io);
            ObjectValue responseObj = (ObjectValue)jsonParser.getJsonObject();
            Iterator<String> users = responseObj.getKeys();
            while (users.hasNext())
            {
               String userName = users.next();
               String email = responseObj.getElement(userName).getStringValue();
               result.put(userName,email);
            }
          return result;  
         }
         else
         {
            String err = readText(connection.getErrorStream());
            String msg = 
            ("Unable to get administrators list from workspace " + tName 
               + " - HTTP status:" + connection.getResponseCode()
               + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
            LOG.error(msg);
            sendAdminErrorEmail(msg, null);
            throw new CloudAdminException("A problem happened during processsing this request. It was reported to developers. Please, try again later.");
         }
      }
      catch (MalformedURLException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");

      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");

      }
      catch (JsonException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");

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
      String mailTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_TEMPLATE, null);
      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_SUBJECT),
            mailTemplate, props, false);
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - join email not send.", e);
         LOG.error("Configuration error - join email not send.", e);
      }
   }
   
   public void sendCreationQueuedEmails(String tName, String userMail, Map<String, String> props)
      throws CloudAdminException
   {

      String userTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_TEMPLATE, null);
      String devTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_DEVELOPERS_TEMPLATE, null);
      try
      {
         String email = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_SUPPOR_EMAIL);
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_SUBJECT),
            userTemplate, props, false);
         mailSender.sendMail(email, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_DEVELOPERS_SUBJECT).replace("${workspace}", tName),
            devTemplate, props, false);
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - creation queued emails is not send", e);
         LOG.error("Configuration error - creation queued emails is not send", e);
      }
   }
   
   
   public void sendCreationRejectedEmail(String tName, String userMail, Map<String, String> props)
      throws CloudAdminException
   {

      String userTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_REJECTED_TEMPLATE, null);
      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_REJECTED_SUBJECT),
            userTemplate, props, false);
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - creation rejected emails is not send", e);
         LOG.error("Configuration error - creation rejected emails is not send", e);
      }

   }

   public void sendJoinRejectedEmails(String tName, String userMail, Map<String, String> props) throws CloudAdminException
   {
      String userTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE, null);
      String salesEmail = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_SALES_EMAIL, null);
      String salesTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE, null);

      try
      {
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT),
            userTemplate, props, false);
         mailSender.sendMail(
            salesEmail,
            cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT).replace("${company}",
               props.get("tenant.repository.name")), salesTemplate, props, true);

         Map<String,String> adminEmails = getTenantAdministrators(tName);
         Iterator<String> it = adminEmails.keySet().iterator(); 
         while (it.hasNext())
         {
            String username = it.next();
            if (username.equals("root"))//Dont send those emails to root CLDINT-184
               continue;
            String adminEmail = adminEmails.get(username);
            props.put("admin.firstname", username);
            if (adminEmail != null)
               mailSender.sendMail(adminEmail,
                  cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT).replace("${company}",
                     props.get("tenant.repository.name")), ownerTemplate, props, false);
         }
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - join rejected emails is not send", e);
         LOG.error("Configuration error - join rejected emails is not send", e);
      }
   }

   public void sendUserJoinedEmails(String tName, String firstName, String userMail, Map<String, String> props)
      throws CloudAdminException
   {
      String userTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE, null);
      String ownerTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE, null);
      String ownerSubject =
         cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT).replace("${company}", tName);
      ownerSubject = ownerSubject.replace("${firstname}", firstName);
      try
      {
         Map<String,String> adminEmails = getTenantAdministrators(tName);
         mailSender.sendMail(userMail, cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT)
            .replace("${company}", tName), userTemplate, props, false);
         Iterator<String> it =adminEmails.keySet().iterator(); 
         while (it.hasNext())
         {
            String username = it.next();
            if (username.equals("root"))//Dont send those emails to root CLDINT-184
               continue;
            String adminEmail = adminEmails.get(username);
            props.put("admin.firstname", username);
            if (adminEmail != null)
               mailSender.sendMail(adminEmail, ownerSubject, ownerTemplate, props, false);
         }
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - user joined but notification emails is not send.", e);
         LOG.error("Configuration error - user joined but notification emails is not send.", e);
      }
   }

   public void sendIntranetCreatedEmail(String userMail, Map<String, String> props) throws CloudAdminException
   {
      String userTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE, null);
      try
      {
         mailSender.sendMail(
            userMail,
            cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT).replace("${company}",
               props.get("tenant.repository.name")), userTemplate, props, false);
      }
      catch (ConfigurationParameterNotFound e)
      {
         sendAdminErrorEmail("Configuration error - workspace created but owner email in not send", e);
         LOG.error("Configuration error - workspace created but owner email in not send", e);
      }
   }

   public void sendAdminErrorEmail(String msg, Exception error)
   {
      String mailTemplate = cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE, null);
      String mailSubject =
         cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT, "Cloud admin error");
      msg = msg.replaceAll("(\r\n|\n\r|\r|\n)", "<br>");

      Map<String, String> props = new HashMap<String, String>();
      props.put("message", msg);

      if (error != null)
      {
         String prettyMsg = error.getMessage().replaceAll("(\r\n|\n\r|\r|\n)", "<br>");
         prettyMsg = prettyMsg.replaceAll("(\t)", "&nbsp;&nbsp;&nbsp;&nbsp;");
         props.put("exception.message", prettyMsg);

         StringBuilder trace = new StringBuilder();
         for (StackTraceElement item : error.getStackTrace())
         {
            String line = item.toString();
            if (line.startsWith("at "))
            {
               trace.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            trace.append(item.toString());
            trace.append("<br>");
         }
         props.put("stack.trace", trace.toString());
      }
      else
      {
         props.put("exception.message", "No exception message provided");
         props.put("stack.trace", "No stack trace provided");
      }
      try
      {
         for (String email : cloudAdminConfiguration.getProperty(CLOUD_ADMIN_MAIL_ADMIN_EMAIL).split(","))
         {
            mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props, true);
         }
      }
      catch (CloudAdminException ex)
      {
         LOG.error("Cannot send mail message to admin. Message was '" + msg + "' it is caused by '"
            + (error != null ? error.getMessage() : "[ no error message provided]") + "'.", ex);
      }
   }
   
   public void sendContactUsEmail(String userMail, String firstName, String subject, String text){
      
      String mailTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_CONTACT_TEMPLATE, "");
      String email = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_SUPPOR_EMAIL, "support@cloud-workspaces.com");
      
      Map<String, String> props = new HashMap<String, String>();
      props.put("user.mail", userMail);
      props.put("user.name", firstName);
      props.put("message", text);
      try
      {
            mailSender.sendMail(email, 
                                 "Contact-Us message submitted: " + subject, 
                                 mailTemplate, props, false, userMail);
      }
      catch (CloudAdminException ex)
      {
       String msg = ("Cannot send mail contactUs message. Message was : <<" + text + ">>. Sender email is: " + userMail);
       LOG.error(msg);
       sendAdminErrorEmail(msg, ex);
      }
   }
   
   public void sendPasswordRestoreEmail(String email, String tName, String uuid){
      
      String mailTemplate = cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_TEMPLATE, "");
      Map<String, String> props = new HashMap<String, String>();
      props.put("user.mail", email);
      props.put("uid", uuid);
      props.put("tenant.repository.name", tName);
      try
      {
            mailSender.sendMail(email, 
               cloudAdminConfiguration.getProperty(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_SUBJECT), 
                                 mailTemplate, props, false, "noreply@cloud-workspaces.com");
      }
      catch (CloudAdminException ex)
      {
       String msg = ("Cannot send mail password restore message. Requestor email is: " + email);
       LOG.error(msg);
       sendAdminErrorEmail(msg, ex);
      }
      
   }

   public String getTenantNameFromWhitelist(String email) throws CloudAdminException
   {
      String tail = email.substring(email.indexOf("@") + 1);
      if (whiteListConfigurationFile == null)
      {
         String tName = tail.substring(0,tail.indexOf("."));
         if (!isInBlackList(email))
         {
            return tName;
         }
         else
         {
            return null;
         }
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
         tName = tail.substring(0,tail.indexOf("."));
         LOG.info("White list file not found, allowing tenant " + tName + "from email:" + email);
         return tName;
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      return tName;
   }
   
   public boolean isInBlackList(String email){
      String tail = email.substring(email.indexOf("@") + 1);
      if (blackListConfigurationFolder == null || (cloudAdminConfiguration.getProperty("cloud.admin.blacklist.file", null)) == null)
      {
         String tName = tail.substring(0,tail.indexOf("."));
         LOG.info("Black list not configured, allowing tenant " + tName + " from email:" + email);
         return false;
      }
      File blacklistFolder = new File(blackListConfigurationFolder);
      if (!blacklistFolder.exists())
        return false;
      try
      {
         File propertyFile = new File(blacklistFolder + "/" + cloudAdminConfiguration.getProperty("cloud.admin.blacklist.file"));
         FileInputStream io = new FileInputStream(propertyFile);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         return properties.containsKey(tail);
      }
      catch (FileNotFoundException e)
      {
         String tName = tail.substring(0,tail.indexOf("."));
         LOG.info("Black list file not found, allowing tenant " + tName + " from email:" + email);
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);      
      }
      return false;
   }
   
   public void putInBlackList(String email){
      String tail = email.substring(email.indexOf("@") + 1);
      if (blackListConfigurationFolder == null || (cloudAdminConfiguration.getProperty("cloud.admin.blacklist.file", null)) == null)
      {
         String msg = "Blacklist action failed - blacklist folder/file not configured, cannot add new record for " + tail;
         LOG.warn(msg);
         sendAdminErrorEmail(msg, null);
         return;
      }
      File blacklistFolder = new File(blackListConfigurationFolder);
      if (!blacklistFolder.exists())
         blacklistFolder.mkdir();
      try
      {
         File propertyFile = new File(blacklistFolder + "/" + cloudAdminConfiguration.getProperty("cloud.admin.blacklist.file"));
         if (!propertyFile.exists())
            propertyFile.createNewFile();
         FileInputStream io = new FileInputStream(propertyFile);
         Properties properties = new Properties();
         properties.load(io);
         io.close();
         if(properties.containsKey(tail))
         {
            return;
         }
         else 
         {
            properties.setProperty(tail, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            properties.store(new FileOutputStream(propertyFile), "");
         }
         LOG.info("Registrations from " + tail + " was blacklisted.");
      }
      catch (FileNotFoundException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
      }
      catch (ConfigurationParameterNotFound e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);      
      }
   }

   public int getMaxUsersForTenant(String tName) throws CloudAdminException
   {
      if (maxUsersConfigurationFile == null)
      {
         return Integer.parseInt(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_TENANT_MAXUSERS, "20"));
      }
      String value = null;
      int count;
      File propertyFile = new File(maxUsersConfigurationFile);
      try
      {
         FileInputStream io = new FileInputStream(propertyFile);
         Properties properties = new Properties();
         properties.load(io);
         value = properties.getProperty(tName);
         if (value == null || value.equals(""))
         {
            return Integer.parseInt(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_TENANT_MAXUSERS, "20"));
         }
         else
         {
            count = Integer.parseInt(value);
         }
         io.close();
      }
      catch (FileNotFoundException e)
      {
         return Integer.parseInt(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_TENANT_MAXUSERS, "20"));
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
         sendAdminErrorEmail(e.getMessage(), e);
         throw new CloudAdminException("An problem happened during processsing this request. It was reported to developers. Please, try again later.");
      }
      return count;
   }
   
   public void joinAll(String tName, RequestState state) throws CloudAdminException
   {
      List<UserRequest> list = requestDao.search(tName, state);
      for (UserRequest one : list)
      {
         String tenant = one.getTenantName();
         String userMail = one.getUserEmail();
         String fName = one.getFirstName();
         String lName = one.getLastName();
         String username = userMail.substring(0, (userMail.indexOf("@")));

         //Whose who only signed up and stopped on limit - sending them join links
         if (one.getState().equals(RequestState.WAITING_LIMIT) && one.getPassword().equals(""))
         {
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.repository.name", tenant);
            props.put("user.mail", userMail);
            props.put("rfid",
               new ReferencesManager(cloudAdminConfiguration).putEmail(userMail, UUID.randomUUID().toString()));
            if (isNewUserAllowed(tenant, username))
            {
               LOG.info("Sending join letter to " + userMail + " - his tenant is raised user limit.");
               sendOkToJoinEmail(userMail, props);
               requestDao.delete(one);
               continue;
            }
            else
            {
               //Do nothing, limit is low
               continue;
            }
         }

         //Whose who only signed up on tenant suspend - sending them join links
         if (one.getState().equals(RequestState.WAITING_JOIN) && one.getPassword().equals(""))
         {
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.repository.name", tenant);
            props.put("user.mail", userMail);
            props.put("rfid",
               new ReferencesManager(cloudAdminConfiguration).putEmail(userMail, UUID.randomUUID().toString()));
            try
            {
               if (isNewUserAllowed(tenant, username))
               {
                  LOG.info("Sending join letter to " + userMail + " - his tenant is resumed.");
                  sendOkToJoinEmail(userMail, props);
                  requestDao.delete(one);
                  continue;
               }
               else
               {
                  //Changing type from WAILTING_JOIN to WAITING_LIMIT
                  UserRequest two =
                     new UserRequest("", one.getTenantName(), one.getUserEmail(), one.getFirstName(),
                        one.getLastName(), one.getCompanyName(), one.getPhone(), one.getPassword(),
                        one.getConfirmationId(), one.isAdministrator(), RequestState.WAITING_LIMIT);
                  requestDao.delete(one);
                  try
                  {
                     Thread.sleep(500);
                  }
                  catch (InterruptedException e)
                  {
                     LOG.warn(e.getMessage());
                  }
                  requestDao.put(two);
                  props.put("users.maxallowed", Integer.toString(getMaxUsersForTenant(tenant)));
                  sendJoinRejectedEmails(tenant, userMail, props);
                  continue;
               }
            }
            catch (UserAlreadyExistsException e)
            {
               //User already exist. do nothing;
               requestDao.delete(one);
            }
         }

         try
         {
            // Checking status
            if (!holder.getTenantStatus(tenant).getState().equals(TenantState.ONLINE))
            {
               String msg = "Tenant " + tenant + " is not online, auto join skipped for user " + userMail;
               LOG.warn(msg);
               continue;
            }
            //Looking for administrators first
            if (one.isAdministrator())
            {
               // Prepare properties for mailing
               Map<String, String> props = new HashMap<String, String>();
               props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
               props.put("tenant.repository.name", tenant);
               props.put("user.mail", userMail);
               props.put("user.name", username);
               props.put("first.name", fName);
               props.put("last.name", lName);
               LOG.info("Joining administrator " + userMail + " to tenant " + tenant);
               storeUser(tenant, userMail, fName, lName, one.getPassword(), true);
               sendIntranetCreatedEmail(userMail, props);
               requestDao.delete(one);
            }
            else
            {
               continue;
            }
         }
         catch (CloudAdminException e)
         {
            String msg =
               ("An problem happened during creating administrator " + userMail + "  on tenant " + " : " + tenant + e
                  .getMessage());
            LOG.error(msg, e);
            sendAdminErrorEmail(msg, null);
         }
      }

      //Now regular users
      List<UserRequest> list2 = requestDao.search(tName, state);
      for (UserRequest one : list2)
      {
         String tenant = one.getTenantName();
         String userMail = one.getUserEmail();
         String fName = one.getFirstName();
         String lName = one.getLastName();
         String username = userMail.substring(0, (userMail.indexOf("@")));

         try
         {
            // Checking status
            if (!holder.getTenantStatus(tenant).getState().equals(TenantState.ONLINE))
            {
               String msg = "Tenant " + tenant + " is not online, auto join skipped for user " + userMail;
               LOG.warn(msg);
               continue;
            }
            // Prepare properties for mailing
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.repository.name", tenant);
            props.put("user.mail", userMail);
            props.put("user.name", username);
            props.put("first.name", fName);
            props.put("last.name", lName);

            if (one.isAdministrator() || one.getPassword().equals(""))
            {
               continue;
            }
            else
            {
               if (isNewUserAllowed(tenant, username))
               {
                  // Storing user & sending appropriate mails
                  LOG.info("Joining user " + userMail + " to tenant " + tenant);
                  storeUser(tenant, userMail, fName, lName, one.getPassword(), false);
                  sendUserJoinedEmails(tenant, fName, userMail, props);
                  requestDao.delete(one);
               }
               else
               {
                  // Limit reached
                  props.put("users.maxallowed", Integer.toString(getMaxUsersForTenant(tenant)));
                  sendJoinRejectedEmails(tenant, userMail, props);
                  //Changing type from WAILTING_JOIN to WAITING_LIMIT
                  UserRequest two =
                     new UserRequest("", one.getTenantName(), one.getUserEmail(), one.getFirstName(),
                        one.getLastName(), one.getCompanyName(), one.getPhone(), one.getPassword(),
                        one.getConfirmationId(), one.isAdministrator(), RequestState.WAITING_LIMIT);
                  requestDao.delete(one);
                  try
                  {
                     Thread.sleep(500);
                  }
                  catch (InterruptedException e)
                  {
                     LOG.warn(e.getMessage());
                  }
                  requestDao.put(two);
               }
            }
         }
         catch (CloudAdminException e)
         {
            String msg =
               ("An problem happened during creating user " + userMail + "  on tenant " + " : " + tenant + e
                  .getMessage());
            LOG.error(msg, e);
            sendAdminErrorEmail(msg, null);
         }
      }
   }
   

   public boolean validateEmail(String aEmailAddress){
      if (aEmailAddress == null) return false;
      boolean result = true;
      try {
        InternetAddress emailAddr = new InternetAddress(aEmailAddress);
        if (!hasNameAndDomain(aEmailAddress)) {
          result = false;
        }
      }
      catch (AddressException ex){
        result = false;
      }
      return result;
    }

    private static boolean hasNameAndDomain(String aEmailAddress){
      String[] tokens = aEmailAddress.split("@");
      return 
       tokens.length == 2 &&
       tokens[0].trim().length() > 0 && 
       tokens[1].trim().length() > 0;
    }
    
    
    public boolean validateUUID(String aEmailAddress, String UUID) throws CloudAdminException{
       String hash = new ReferencesManager(cloudAdminConfiguration).getHash(aEmailAddress);
       if (hash == null)
          return false;
       else
        return hash.equals(UUID);
    }
    
    
   public void resumeTenant(String tName) throws CloudAdminException
   {
      TenantResumeThread thread =
               new TenantResumeThread(cloudAdminConfiguration, tName);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(thread);
      
   }

   /**
    * Read text message from InputStream. 
    * @param errStream InputStream
    * @return String
    * @throws IOException 
    */
   protected String readText(InputStream errStream) throws IOException
   {
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
   
   public Map<String, String[]> sortByComparator(Map<String, String[]> unsortMap)
   {

      List<String> list = new LinkedList<String>(unsortMap.keySet());
      //sort list based on comparator
      Collections.sort(list, Collections.reverseOrder(new Comparator<String>()
      {
         public int compare(String o1, String o2)
         {
            Long f1 = Long.valueOf(o1.substring(o1.indexOf("_")+1));
            Long f2 = Long.valueOf(o2.substring(o2.indexOf("_")+1));
            return f1.compareTo(f2);
         }
      }));
      //put sorted list into map again
      Map<String, String[]> sortedMap = new LinkedHashMap<String, String[]>();
      for (Iterator<String> it = list.iterator(); it.hasNext();)
      {
         String key = (String)it.next();
         sortedMap.put(key, unsortMap.get(key));
      }
      return sortedMap;
   }
}
