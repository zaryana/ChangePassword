/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_TENANT_QUEUE_DIR;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantRegistrationException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.tenant.TenantNameValidator;
import org.exoplatform.cloudmanagement.admin.tenant.UserMailValidator;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

public class NotificationMailSender {

  private static final Logger                          LOG = LoggerFactory.getLogger(NotificationMailSender.class);

  private final Configuration                          cloudAdminConfiguration;

  private final WorkspacesMailSender                   mailSender;

  private final WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;
  
  private final EmailValidationStorage emailValidationStorage;
  
  private final TenantNameValidator tenantNameValidator;
  
  private final UserMailValidator userMailValidator;

  public NotificationMailSender(Configuration cloudAdminConfiguration,
                                WorkspacesMailSender mailSender,
                                WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                                EmailValidationStorage emailValidationStorage,
                                TenantNameValidator tenantNameValidator,
                                UserMailValidator userMailValidator) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.mailSender = mailSender;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.emailValidationStorage = emailValidationStorage;
    this.tenantNameValidator = tenantNameValidator;
    this.userMailValidator = userMailValidator;
  }

  public void sendOkToJoinEmail(String userMail, Map<String, String> props) throws CloudAdminException {
    String mailTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_TEMPLATE,
                                                            null);
    try {
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_SUBJECT),
                          mailTemplate,
                          props,
                          false);
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - join email not send.", e);
      LOG.error("Configuration error - join email not send.", e);
    }
  }

  /*
  public void sendCreationQueuedEmails(String tName, String userMail, Map<String, String> props) throws CloudAdminException {

    String userTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_TEMPLATE,
                                                            null);
    String devTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_DEVELOPERS_TEMPLATE,
                                                           null);
    try {
      String email = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_SUPPOR_EMAIL);
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_SUBJECT),
                          userTemplate,
                          props,
                          false);
      mailSender.sendMail(email,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_QUEUED_DEVELOPERS_SUBJECT)
                                                 .replace("${workspace}", tName),
                          devTemplate,
                          props,
                          false);
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - creation queued emails is not send", e);
      LOG.error("Configuration error - creation queued emails is not send", e);
    }
  }

  public void sendCreationRejectedEmail(String tName, String userMail, Map<String, String> props) throws CloudAdminException {

    String userTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_REJECTED_TEMPLATE,
                                                            null);
    try {
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_REQUEST_REJECTED_SUBJECT),
                          userTemplate,
                          props,
                          false);
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - creation rejected emails is not send", e);
      LOG.error("Configuration error - creation rejected emails is not send", e);
    }

  }
  */
  public void sendJoinRejectedEmails(String tName, String userMail, Map<String, String> props) throws CloudAdminException {
    String userTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_TEMPLATE,
                                                            null);
    String ownerTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_TEMPLATE,
                                                             null);
    String salesEmail = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_SALES_EMAIL,
                                                          null);
    String salesTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_TEMPLATE,
                                                             null);

    try {
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_USER_SUBJECT),
                          userTemplate,
                          props,
                          false);
      mailSender.sendMail(salesEmail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_SALES_SUBJECT)
                                                 .replace("${company}",
                                                          props.get("tenant.repository.name")),
                          salesTemplate,
                          props,
                          true);

      Map<String, String> adminEmails = workspacesOrganizationRequestPerformer.getTenantAdministrators(tName);
      Iterator<String> it = adminEmails.keySet().iterator();
      while (it.hasNext()) {
        String username = it.next();
        if (username.equals("root"))// Dont send those emails to root CLDINT-184
          continue;
        String adminEmail = adminEmails.get(username);
        props.put("admin.firstname", username);
        if (adminEmail != null)
          mailSender.sendMail(adminEmail,
                              cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_JOIN_CLOSED_OWNER_SUBJECT)
                                                     .replace("${company}",
                                                              props.get("tenant.repository.name")),
                              ownerTemplate,
                              props,
                              false);
      }
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - join rejected emails is not send", e);
      LOG.error("Configuration error - join rejected emails is not send", e);
    }
  }
  
  public void sendUserJoinedEmails(String tName,
                                   String firstName,
                                   String userMail,
                                   Map<String, String> props) throws CloudAdminException {
    String userTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_TEMPLATE,
                                                            null);
    String ownerTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_TEMPLATE,
                                                             null);
    String ownerSubject = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_OWNER_SUBJECT)
                                                 .replace("${company}", tName);
    ownerSubject = ownerSubject.replace("${firstname}", firstName);
    try {
      Map<String, String> adminEmails = workspacesOrganizationRequestPerformer.getTenantAdministrators(tName);
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_JOINED_SUBJECT)
                                                 .replace("${company}", tName),
                          userTemplate,
                          props,
                          false);
      Iterator<String> it = adminEmails.keySet().iterator();
      while (it.hasNext()) {
        String username = it.next();
        if (username.equals("root"))// Dont send those emails to root CLDINT-184
          continue;
        String adminEmail = adminEmails.get(username);
        props.put("admin.firstname", username);
        if (adminEmail != null)
          mailSender.sendMail(adminEmail, ownerSubject, ownerTemplate, props, false);
      }
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - user joined but notification emails is not send.",
                          e);
      LOG.error("Configuration error - user joined but notification emails is not send.", e);
    }
  }

  public void sendIntranetCreatedEmail(String userMail, Map<String, String> props) throws CloudAdminException {
    String userTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_TEMPLATE,
                                                            null);
    try {
      mailSender.sendMail(userMail,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_USER_INTRANET_CREATED_SUBJECT)
                                                 .replace("${company}",
                                                          props.get("tenant.repository.name")),
                          userTemplate,
                          props,
                          false);
    } catch (CloudAdminException e) {
      sendAdminErrorEmail("Configuration error - workspace created but owner email in not send", e);
      LOG.error("Configuration error - workspace created but owner email in not send", e);
    }
  }

  public void sendAdminErrorEmail(String msg, Exception error) {
    String mailTemplate = cloudAdminConfiguration.getString(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE,
                                                            null);
    String mailSubject = cloudAdminConfiguration.getString(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT,
                                                           "Cloud admin error");
    msg = msg.replaceAll("(\r\n|\n\r|\r|\n)", "<br>");

    Map<String, String> props = new HashMap<String, String>();
    props.put("message", msg);

    if (error != null) {
      String prettyMsg = error.getMessage().replaceAll("(\r\n|\n\r|\r|\n)", "<br>");
      prettyMsg = prettyMsg.replaceAll("(\t)", "&nbsp;&nbsp;&nbsp;&nbsp;");
      props.put("exception.message", prettyMsg);

      StringBuilder trace = new StringBuilder();
      for (StackTraceElement item : error.getStackTrace()) {
        String line = item.toString();
        if (line.startsWith("at ")) {
          trace.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }
        trace.append(item.toString());
        trace.append("<br>");
      }
      props.put("stack.trace", trace.toString());
    } else {
      props.put("exception.message", "No exception message provided");
      props.put("stack.trace", "No stack trace provided");
    }
    try {
      for (String email : cloudAdminConfiguration.getStringArray(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL))
                                                 //.split(","))
      {
        mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props, true);
      }
    } catch (CloudAdminException ex) {
      LOG.error("Cannot send mail message to admin. Message was '" + msg + "' it is caused by '"
          + (error != null ? error.getMessage() : "[ no error message provided]") + "'.", ex);
    }
  }

  public void sendContactUsEmail(String userMail, String firstName, String subject, String text) {

    String mailTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_CONTACT_TEMPLATE,
                                                            "");
    String email = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_SUPPOR_EMAIL,
                                                     "support@cloud-workspaces.com");

    Map<String, String> props = new HashMap<String, String>();
    props.put("user.mail", userMail);
    props.put("user.name", firstName);
    props.put("message", text);
    try {
      mailSender.sendMail(email,
                          "Contact-Us message submitted: " + subject,
                          mailTemplate,
                          props,
                          false,
                          userMail);
    } catch (CloudAdminException ex) {
      String msg = ("Cannot send mail contactUs message. Message was : <<" + text
          + ">>. Sender email is: " + userMail);
      LOG.error(msg);
      sendAdminErrorEmail(msg, ex);
    }
  }

  public void sendPasswordRestoreEmail(String email, String tName, String uuid) throws CloudAdminException {

    String mailTemplate = cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_TEMPLATE,
                                                            "");
    Map<String, String> props = new HashMap<String, String>();
    props.put("user.mail", email);
    props.put("uid", uuid);
    props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
    props.put("tenant.repository.name", tName);
    try {
      mailSender.sendMail(email,
                          cloudAdminConfiguration.getString(MailingProperties.CLOUD_ADMIN_MAIL_PASSWORD_RESTORE_SUBJECT),
                          mailTemplate,
                          props,
                          false,
                          "noreply@cloud-workspaces.com");
    } catch (CloudAdminException ex) {
      String msg = ("Cannot send mail password restore message. Requestor email is: " + email);
      LOG.error(msg);
      sendAdminErrorEmail(msg, ex);
    }

  }
  
  /**
   * Send custom email to all owners of tenants on validation.
   * 
   * @param emailTemplate String
   * @param subject String
   * @throws CloudAdminException if cannot read validation storage
   */
  public void sendEmailToValidation(String emailTemplate, String subject) throws CloudAdminException {

    int counter = 0;
    StringBuilder info = new StringBuilder();

    final String confDir = System.getProperty("cloud.admin.configuration.dir");

    LOG.info("Sending custom email '" + subject + "' to users from validation queue.");

    final File tenantQueueDir = new File(cloudAdminConfiguration.getString(CLOUD_ADMIN_TENANT_QUEUE_DIR),
                                         "validation");
    if (!tenantQueueDir.exists()) {
      LOG.error("Queue storage " + tenantQueueDir.getAbsolutePath() + " not found");
      throw new CloudAdminException(500, "Cannot read queue storage. Contact administrators.");
    }

    String listForTenantQueueDir[] = tenantQueueDir.list();
    for (String id : listForTenantQueueDir) {

      String uuid = id.substring(0, id.indexOf('.'));

      Map<String, String> validationData = emailValidationStorage.getValidationData(uuid);

      String tenantName = validationData.get(TenantInfoFieldName.PROPERTY_TENANT_NAME);
      String userMail = validationData.get(TenantInfoFieldName.PROPERTY_USER_MAIL);

      try {
        tenantNameValidator.validateTenantName(tenantName);
        userMailValidator.validateUserMail(userMail);

        // send email
        String mailTemplate = confDir + "/" + emailTemplate;
        if (mailTemplate == null) {
          throw new TenantRegistrationException(500,
                                                "Mail template configuration not found. Please contact support.");
        }

        Map<String, String> props = new HashMap<String, String>();
        props.put("tenant.masterhost",
                  AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
        props.put("tenant.name", tenantName);
        props.put("tenant.repository.name", tenantName);
        props.put("user.mail", userMail);
        props.put("id", uuid);

        mailSender.sendMail(userMail, subject, mailTemplate, props, false);

        counter++;
        info.append(userMail);
        info.append(' ');

        try {
          Thread.sleep(1100);
        } catch (Throwable e) {
          LOG.warn("Error of thread sleep in sendCustomEmail: " + e);
        }

      } catch (Exception e) {
        LOG.error("Cannot send custom email '"
            + subject
            + "' to owner of request "
            + id
            + (validationData != null ? " (tenant: " + tenantName + ", email: " + userMail + ")"
                                     : "") + ". Skipping it.", e);
      }
    }
    LOG.info("Custom message sent to tenants on validation. " + "Email '" + subject + "' sent to "
        + counter + " users" + (counter > 0 ? ": " + info.toString() : ""));
  }
}
