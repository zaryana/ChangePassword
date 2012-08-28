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
package com.exoplatform.cloudworkspaces.installer.configuration.updaters;

import com.exoplatform.cloudworkspaces.installer.ConfigUtils;
import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.ConfigurationException;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

import java.io.File;
import java.io.IOException;

public class MailConfigurationUpdater extends BaseConfigurationUpdater {
  private final Question cloudMailHostQuestion      = new Question("cloud.mail.host",
                                                                   "Set host of mail server",
                                                                   null,
                                                                   "^.*$",
                                                                   null);

  private final Question cloudMailPortQuestion      = new Question("cloud.mail.port",
                                                                   "Set port of mail server",
                                                                   "465",
                                                                   "^\\d*$",
                                                                   null);

  private final Question cloudMailSslQuestion       = new Question("cloud.mail.ssl",
                                                                   "Is enable ssl for mail server? (true or false)",
                                                                   "true",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudMailUserQuestion      = new Question("cloud.mail.user",
                                                                   "Set username for mail server",
                                                                   null,
                                                                   "^.*$",
                                                                   null);

  private final Question cloudMailPasswordQuestion  = new Question("cloud.mail.password",
                                                                   "Set password for mail server",
                                                                   null,
                                                                   "^.*$",
                                                                   null);

  private final Question cloudMailSmtpAuthQuestion  = new Question("cloud.mail.smtp.auth",
                                                                   "Is enable smtp auth for mail server? (true or false)",
                                                                   "true",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudAdminEmailQuestion    = new Question("cloud.admin.email",
                                                                   "Set cloud admin email",
                                                                   "noreply@cloud-intranet.com",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudLoggerEmailQuestion   = new Question("cloud.logger.email",
                                                                   "Set logger email",
                                                                   "logs@cloud-intranet.com",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudSupportEmailQuestion  = new Question("cloud.support.email",
                                                                   "Set support email",
                                                                   "support@cloud-intranet.com",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudSupportSenderQuestion = new Question("cloud.support.sender",
                                                                   "Set support sender",
                                                                   "support@cloud-intranet.com",
                                                                   "^.*$",
                                                                   null);

  private final Question cloudSalesEmailQuestion    = new Question("cloud.sales.email",
                                                                   "Set sales email",
                                                                   "exo-sales@exoplatform.com",
                                                                   "^.*$",
                                                                   null);

  @Override
  public void update(File confDir,
                     File tomcatDir,
                     File previousConfDir,
                     File previousTomcatDir,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    try {
      interaction.println("");
      interaction.println("");
      interaction.println("Mail server settings");
      answers.addBlockName("Mail server settings");

      String prevCloudMailHost = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                          "environment.sh",
                                                          "CLOUD_MAIL_HOST");
      String prevCloudMailPort = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                          "environment.sh",
                                                          "CLOUD_MAIL_PORT");
      String prevCloudMailSsl = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                         "environment.sh",
                                                         "CLOUD_MAIL_SSL");
      String prevCloudMailUser = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                          "environment.sh",
                                                          "CLOUD_MAIL_USER");
      String prevCloudMailPassword = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                              "environment.sh",
                                                              "CLOUD_MAIL_PASSWORD");
      String prevCloudMailSmtpAuth = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                              "environment.sh",
                                                              "CLOUD_MAIL_SMTP_AUTH");

      clearBlock();
      addToBlock(cloudMailHostQuestion, prevCloudMailHost);
      addToBlock(cloudMailPortQuestion, prevCloudMailPort);
      addToBlock(cloudMailSslQuestion, prevCloudMailSsl);
      addToBlock(cloudMailSmtpAuthQuestion, prevCloudMailSmtpAuth);
      addToBlock(cloudMailUserQuestion, prevCloudMailUser);
      addToBlock(cloudMailPasswordQuestion, prevCloudMailPassword);

      boolean usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String mailHost = prevCloudMailHost;
      String mailPort = prevCloudMailPort;
      String mailSsl = prevCloudMailSsl;
      String mailSmtpAuth = prevCloudMailSmtpAuth;
      String mailUser = prevCloudMailUser;
      String mailPassword = prevCloudMailPassword;
      if (!usePrev) {
        cloudMailHostQuestion.setDefaults(prevCloudMailHost);
        cloudMailPortQuestion.setDefaults(prevCloudMailPort);
        cloudMailSslQuestion.setDefaults(prevCloudMailSsl);
        cloudMailUserQuestion.setDefaults(prevCloudMailUser);
        cloudMailPasswordQuestion.setDefaults(prevCloudMailPassword);
        cloudMailSmtpAuthQuestion.setDefaults(prevCloudMailSmtpAuth);

        mailHost = interaction.ask(cloudMailHostQuestion);
        mailPort = interaction.ask(cloudMailPortQuestion);
        mailSsl = interaction.ask(cloudMailSslQuestion);
        mailUser = interaction.ask(cloudMailUserQuestion);
        mailPassword = interaction.ask(cloudMailPasswordQuestion);
        mailSmtpAuth = interaction.ask(cloudMailSmtpAuthQuestion);
      }
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_HOST",
                                mailHost);
      answers.addAnswer(cloudMailHostQuestion, mailHost);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_PORT",
                                mailPort);
      answers.addAnswer(cloudMailPortQuestion, mailPort);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_SSL",
                                mailSsl);
      answers.addAnswer(cloudMailSslQuestion, mailSsl);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_USER",
                                mailUser);
      answers.addAnswer(cloudMailUserQuestion, mailUser);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_PASSWORD",
                                mailPassword);
      answers.addAnswer(cloudMailPasswordQuestion, mailPassword);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_MAIL_SMTP_AUTH",
                                mailSmtpAuth);
      answers.addAnswer(cloudMailSmtpAuthQuestion, mailSmtpAuth);

      String prevCloudAdminEmail = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                            "environment.sh",
                                                            "CLOUD_ADMIN_EMAIL");
      String prevCloudLoggerEmail = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                             "environment.sh",
                                                             "CLOUD_LOGGER_EMAIL");
      String prevCloudSupportEmail = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                              "environment.sh",
                                                              "CLOUD_SUPPORT_EMAIL");
      String prevCloudSupportSender = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                               "environment.sh",
                                                               "CLOUD_SUPPORT_SENDER");
      String prevCloudSalesEmail = ConfigUtils.findProperty(new File(previousTomcatDir, "bin"),
                                                            "environment.sh",
                                                            "CLOUD_SALES_EMAIL");

      clearBlock();
      addToBlock(cloudAdminEmailQuestion, prevCloudAdminEmail);
      addToBlock(cloudLoggerEmailQuestion, prevCloudLoggerEmail);
      addToBlock(cloudSupportEmailQuestion, prevCloudSupportEmail);
      addToBlock(cloudSupportSenderQuestion, prevCloudSupportSender);
      addToBlock(cloudSalesEmailQuestion, prevCloudSalesEmail);

      usePrev = false;
      if (wasBlockChanged()) {
        usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
      }

      String adminEmail = prevCloudAdminEmail;
      String loggerEmail = prevCloudLoggerEmail;
      String supportEmail = prevCloudSupportEmail;
      String supportSender = prevCloudSupportSender;
      String salesEmail = prevCloudSalesEmail;
      if (!usePrev) {
        cloudAdminEmailQuestion.setDefaults(prevCloudAdminEmail);
        cloudLoggerEmailQuestion.setDefaults(prevCloudLoggerEmail);
        cloudSupportEmailQuestion.setDefaults(prevCloudSupportEmail);
        cloudSupportSenderQuestion.setDefaults(prevCloudSupportSender);
        cloudSalesEmailQuestion.setDefaults(prevCloudSalesEmail);

        adminEmail = interaction.ask(cloudAdminEmailQuestion);
        loggerEmail = interaction.ask(cloudLoggerEmailQuestion);
        supportEmail = interaction.ask(cloudSupportEmailQuestion);
        supportSender = interaction.ask(cloudSupportSenderQuestion);
        salesEmail = interaction.ask(cloudSalesEmailQuestion);
      }
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_ADMIN_EMAIL",
                                adminEmail);
      answers.addAnswer(cloudAdminEmailQuestion, adminEmail);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_LOGGER_EMAIL",
                                loggerEmail);
      answers.addAnswer(cloudLoggerEmailQuestion, loggerEmail);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_SUPPORT_EMAIL",
                                supportEmail);
      answers.addAnswer(cloudSupportEmailQuestion, supportEmail);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_SUPPORT_SENDER",
                                supportSender);
      answers.addAnswer(cloudSupportSenderQuestion, supportSender);
      ConfigUtils.writeQuotedProperty(new File(tomcatDir, "bin"),
                                "environment.sh",
                                "CLOUD_SALES_EMAIL",
                                salesEmail);
      answers.addAnswer(cloudSalesEmailQuestion, salesEmail);
    } catch (IOException e) {
      throw new ConfigurationException("Updating mail configuration failed", e);
    }
  }

}
