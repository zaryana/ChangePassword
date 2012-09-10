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

import com.exoplatform.cloudworkspaces.installer.InstallerException;
import com.exoplatform.cloudworkspaces.installer.configuration.AdminConfiguration;
import com.exoplatform.cloudworkspaces.installer.configuration.BaseConfigurationUpdater;
import com.exoplatform.cloudworkspaces.installer.configuration.CurrentAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousAdmin;
import com.exoplatform.cloudworkspaces.installer.configuration.PreviousQuestion;
import com.exoplatform.cloudworkspaces.installer.configuration.Question;
import com.exoplatform.cloudworkspaces.installer.interaction.AnswersManager;
import com.exoplatform.cloudworkspaces.installer.interaction.InteractionManager;

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
  public void update(PreviousAdmin prevAdmin,
                     CurrentAdmin currAdmin,
                     InteractionManager interaction,
                     AnswersManager answers) throws InstallerException {
    interaction.println("");
    interaction.println("");
    interaction.println("Mail server settings");
    answers.addBlockName("Mail server settings");

    AdminConfiguration prevConfiguration = prevAdmin.getAdminConfiguration();
    AdminConfiguration currConfiguration = currAdmin.getAdminConfiguration();

    String prevCloudMailHost = prevConfiguration.get("cloud.admin.mail.host");
    String prevCloudMailPort = prevConfiguration.get("cloud.admin.mail.port");
    String prevCloudMailUser = prevConfiguration.get("cloud.admin.mail.smtp.auth.username");
    String prevCloudMailPassword = prevConfiguration.get("cloud.admin.mail.smtp.auth.password");
    String prevCloudMailSmtpAuth = prevConfiguration.get("cloud.admin.mail.smtp.auth");

    clearBlock();
    addToBlock(cloudMailHostQuestion, prevCloudMailHost);
    addToBlock(cloudMailPortQuestion, prevCloudMailPort);
    addToBlock(cloudMailSmtpAuthQuestion, prevCloudMailSmtpAuth);
    addToBlock(cloudMailUserQuestion, prevCloudMailUser);
    addToBlock(cloudMailPasswordQuestion, prevCloudMailPassword);

    boolean usePrev = false;
    if (wasBlockChanged()) {
      usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
    }

    String mailHost = prevCloudMailHost;
    String mailPort = prevCloudMailPort;
    String mailSmtpAuth = prevCloudMailSmtpAuth;
    String mailUser = prevCloudMailUser;
    String mailPassword = prevCloudMailPassword;
    if (!usePrev) {
      cloudMailHostQuestion.setDefaults(prevCloudMailHost);
      cloudMailPortQuestion.setDefaults(prevCloudMailPort);
      cloudMailUserQuestion.setDefaults(prevCloudMailUser);
      cloudMailPasswordQuestion.setDefaults(prevCloudMailPassword);
      cloudMailSmtpAuthQuestion.setDefaults(prevCloudMailSmtpAuth);

      mailHost = interaction.ask(cloudMailHostQuestion);
      mailPort = interaction.ask(cloudMailPortQuestion);
      mailUser = interaction.ask(cloudMailUserQuestion);
      mailPassword = interaction.ask(cloudMailPasswordQuestion);
      mailSmtpAuth = interaction.ask(cloudMailSmtpAuthQuestion);
    }
    currConfiguration.set("cloud.admin.mail.host", mailHost);
    currConfiguration.set("cloud.admin.mail.port", mailPort);
    currConfiguration.set("cloud.admin.mail.smtp.auth.username", mailUser);
    currConfiguration.set("cloud.admin.mail.smtp.auth.password", mailPassword);
    currConfiguration.set("cloud.admin.mail.smtp.auth", mailSmtpAuth);
    answers.addAnswer(cloudMailHostQuestion, mailHost);
    answers.addAnswer(cloudMailPortQuestion, mailPort);
    answers.addAnswer(cloudMailUserQuestion, mailUser);
    answers.addAnswer(cloudMailPasswordQuestion, mailPassword);
    answers.addAnswer(cloudMailSmtpAuthQuestion, mailSmtpAuth);

    String prevCloudAdminEmail = prevConfiguration.get("cloud.admin.mail.admin.email");
    String prevCloudSupportEmail = prevConfiguration.get("cloud.admin.mail.support.email");
    String prevCloudSupportSender = prevConfiguration.get("cloud.admin.mail.support.from");
    String prevCloudSalesEmail = prevConfiguration.get("cloud.admin.mail.sales.email");

    clearBlock();
    addToBlock(cloudAdminEmailQuestion, prevCloudAdminEmail);
    addToBlock(cloudSupportEmailQuestion, prevCloudSupportEmail);
    addToBlock(cloudSupportSenderQuestion, prevCloudSupportSender);
    addToBlock(cloudSalesEmailQuestion, prevCloudSalesEmail);

    usePrev = false;
    if (wasBlockChanged()) {
      usePrev = interaction.ask(new PreviousQuestion(getChanges())).equals("yes");
    }

    String adminEmail = prevCloudAdminEmail;
    String supportEmail = prevCloudSupportEmail;
    String supportSender = prevCloudSupportSender;
    String salesEmail = prevCloudSalesEmail;
    if (!usePrev) {
      cloudAdminEmailQuestion.setDefaults(prevCloudAdminEmail);
      cloudSupportEmailQuestion.setDefaults(prevCloudSupportEmail);
      cloudSupportSenderQuestion.setDefaults(prevCloudSupportSender);
      cloudSalesEmailQuestion.setDefaults(prevCloudSalesEmail);

      adminEmail = interaction.ask(cloudAdminEmailQuestion);
      supportEmail = interaction.ask(cloudSupportEmailQuestion);
      supportSender = interaction.ask(cloudSupportSenderQuestion);
      salesEmail = interaction.ask(cloudSalesEmailQuestion);
    }
    currConfiguration.set("cloud.admin.mail.admin.email", adminEmail);
    currConfiguration.set("cloud.admin.mail.support.email", supportEmail);
    currConfiguration.set("cloud.admin.mail.support.from", supportSender);
    currConfiguration.set("cloud.admin.mail.sales.email", salesEmail);

    answers.addAnswer(cloudAdminEmailQuestion, adminEmail);
    answers.addAnswer(cloudSupportEmailQuestion, supportEmail);
    answers.addAnswer(cloudSupportSenderQuestion, supportSender);
    answers.addAnswer(cloudSalesEmailQuestion, salesEmail);
  }

}
