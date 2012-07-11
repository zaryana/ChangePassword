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

import com.exoplatform.cloudworkspaces.dao.ModifiableEmailValidationStorage;
import com.exoplatform.cloudworkspaces.http.WorkspacesOrganizationRequestPerformer;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.TenantRegistrationException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.EmailValidationStorage;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.tenant.TenantNameValidator;
import org.exoplatform.cloudmanagement.admin.tenant.UserMailValidator;
import org.exoplatform.cloudmanagement.admin.util.AdminConfigurationUtil;
import org.exoplatform.cloudmanagement.status.TenantState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NotificationMailSender {

  private static final Logger                          LOG = LoggerFactory.getLogger(NotificationMailSender.class);

  private final Configuration                          cloudAdminConfiguration;

  private final WorkspacesMailSender                   mailSender;

  private final WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer;

  private final EmailValidationStorage                 emailValidationStorage;

  private final TenantNameValidator                    tenantNameValidator;

  private final UserMailValidator                      userMailValidator;

  private TenantInfoDataManager                        tenantInfoDataManager;

  private final ModifiableEmailValidationStorage       modifiableEmailValidationStorage;

  public NotificationMailSender(Configuration cloudAdminConfiguration,
                                WorkspacesMailSender mailSender,
                                WorkspacesOrganizationRequestPerformer workspacesOrganizationRequestPerformer,
                                EmailValidationStorage emailValidationStorage,
                                TenantNameValidator tenantNameValidator,
                                UserMailValidator userMailValidator,
                                TenantInfoDataManager tenantInfoDataManager,
                                ModifiableEmailValidationStorage modifiableEmailValidationStorage) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.mailSender = mailSender;
    this.workspacesOrganizationRequestPerformer = workspacesOrganizationRequestPerformer;
    this.emailValidationStorage = emailValidationStorage;
    this.tenantNameValidator = tenantNameValidator;
    this.userMailValidator = userMailValidator;
    this.tenantInfoDataManager = tenantInfoDataManager;
    this.modifiableEmailValidationStorage = modifiableEmailValidationStorage;
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
      // .split(","))
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
    Set<String> setAliasses = new HashSet<String>();

    final String confDir = System.getProperty("cloud.admin.configuration.dir");

    LOG.info("Sending custom email '" + subject + "' to users from validation queue.");

    try {
      setAliasses = modifiableEmailValidationStorage.getAliases();
    } catch (IOException e) {
      throw new CloudAdminException("Cannot read aliases.", e);
    }
    for (String uuid : setAliasses) {

      Map<String, String> validationData = emailValidationStorage.getValidationData(uuid);

      String tenantName = validationData.get(TenantInfoFieldName.PROPERTY_TENANT_NAME);
      String userMail = validationData.get(TenantInfoFieldName.PROPERTY_USER_MAIL);

      tenantNameValidator.validateTenantName(tenantName);
      userMailValidator.validateUserMail(userMail);

      // send email
      String mailTemplate = confDir + "/" + emailTemplate;
      try {
        sendCustomEmail(userMail, tenantName, uuid, mailTemplate, subject);
      } catch (Exception e) {
        String msg = "Cannot send custom email '"
            + subject
            + "' to owner of request "
            + uuid
            + (validationData != null ? " (tenant: " + tenantName + ", email: " + userMail + ")"
                                     : "") + ". Skipping it.";
        LOG.error(msg, e);
        throw new CloudAdminException(msg);
      }

      counter++;
      info.append(userMail);
      info.append(' ');

      try {
        Thread.sleep(1100);
      } catch (Throwable e) {
        LOG.warn("Error of thread sleep in sendCustomEmail: " + e);
      }

    }
    LOG.info("Custom message sent to tenants on validation. " + "Email '" + subject + "' sent to "
        + counter + " users" + (counter > 0 ? ": " + info.toString() : ""));
  }

  public void sendCustomEmail(String userMail,
                              String tenantName,
                              String uuid,
                              String emailTemplate,
                              String subject) throws CloudAdminException {

    if (emailTemplate == null) {
      throw new TenantRegistrationException(500,
                                            "Mail template configuration not found. Please contact support.");
    }

    Map<String, String> props = new HashMap<String, String>();
    props.put("tenant.masterhost", AdminConfigurationUtil.getMasterHost(cloudAdminConfiguration));
    props.put("tenant.name", tenantName);
    props.put("tenant.repository.name", tenantName);
    props.put("user.mail", userMail);
    props.put("id", uuid);

    mailSender.sendMail(userMail, subject, emailTemplate, props, false);
  }

  /**
   * Send custom email to all owners of tenants with status.
   * 
   * @param emailTemplate String
   * @param subject String
   * @param state String
   * @throws CloudAdminException
   */
  public void sendEmailForTenantsToState(String emailTemplate, String subject, String state) throws CloudAdminException {
    int counter = 0;
    StringBuilder info = new StringBuilder();
    TenantState tState = null;
    final String confDir = System.getProperty("cloud.admin.configuration.dir");
    String mailTemplate = confDir + "/" + emailTemplate;

    if ("all".equalsIgnoreCase(state)) {
      sendEmailToValidation(emailTemplate, subject);
    } else {
      tState = TenantState.valueOf(state.toUpperCase());
    }
    if ("all".equalsIgnoreCase(state) || tState != null) {
      LOG.info("Sending custom email '" + subject + "' to users of " + state + " tenants.");

      Set<String> tenants = tenantInfoDataManager.getNames();

      for (String tenantName : tenants) {
        if ("all".equalsIgnoreCase(state)
            || tState.equals(TenantState.valueOf(tenantInfoDataManager.getValue(tenantName,
                                                                                TenantInfoFieldName.PROPERTY_STATE)))) {
          String userMail = tenantInfoDataManager.getValue(tenantName,
                                                           TenantInfoFieldName.PROPERTY_USER_MAIL);
          String templateId = tenantInfoDataManager.getValue(tenantName,
                                                             TenantInfoFieldName.PROPERTY_TEMPLATE_ID);
          try {
            sendCustomEmail(userMail, tenantName, templateId, mailTemplate, subject);
          } catch (Exception e) {
            String msg = "Cannot send custom email '" + subject + "' to owner of tenant '"
                + tenantName + "'. Skipping it.";
            throw new CloudAdminException(msg);
          }
          counter++;
          info.append(userMail);
          info.append(' ');

          try {
            Thread.sleep(1100);
          } catch (Throwable e) {
            LOG.warn("Error of thread sleep in sendCustomEmail: " + e);
          }
        }
      }
      LOG.info("Send custom mail to users of " + state + " tenants." + " Email '" + subject
          + "' sent to " + counter + " users" + (counter > 0 ? ": " + info.toString() : ""));
    } else
      LOG.error("Cannot send custom email '" + subject + ". Skipping it.");
  }
}
