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
package com.exoplatform.cloudworkspaces.patch;

import com.exoplatform.cloudworkspaces.NotificationMailSender;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration;
import org.exoplatform.cloudworkspaces.patch.utils.WorkspacesErrorMailSender;
import org.exoplatform.cloudworkspaces.patch.utils.WorkspacesErrorMailSenderProvider;
import org.picocontainer.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

/**
 * DON'T USE THIS CLASS.<br>
 * <br>
 * This class needs to help email sending in CM patches.
 */
@Deprecated
public class WorkspacesErrorMailSenderImpl implements WorkspacesErrorMailSender, Startable {

  private static final Logger          LOG = LoggerFactory.getLogger(WorkspacesErrorMailSenderImpl.class);

  private final Configuration          cloudAdminConfiguration;

  private final WorkspacesMailSender   workspacesMailSender;

  private final NotificationMailSender notificationMailSender;

  public WorkspacesErrorMailSenderImpl(Configuration cloudAdminConfiguration,
                                       WorkspacesMailSender workspacesMailSender,
                                       NotificationMailSender notificationMailSender) {
    this.cloudAdminConfiguration = cloudAdminConfiguration;
    this.workspacesMailSender = workspacesMailSender;
    this.notificationMailSender = notificationMailSender;
  }

  @Override
  public void sendErrorToAdmin(String subject, String body) {
    File tmpTemplate = null;
    try {
      tmpTemplate = File.createTempFile("cloud-admin-error-template", ".txt");
      FileOutputStream fout = new FileOutputStream(tmpTemplate);
      try {
        fout.write(body.getBytes());
      } finally {
        fout.close();
      }
      for (String email : cloudAdminConfiguration.getStringArray(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL)) {
        workspacesMailSender.sendMail(email.trim(),
                                      subject,
                                      tmpTemplate.getAbsolutePath(),
                                      Collections.<String, String> emptyMap(),
                                      true);
      }

    } catch (Exception e) {
      LOG.error("Error while sending email to admin", e);
    } finally {
      if (tmpTemplate != null)
        tmpTemplate.deleteOnExit();
    }
  }

  @Override
  public void sendErrorToAdmin(String message, Exception cause) {
    notificationMailSender.sendAdminErrorEmail(message, cause);
  }

  @Override
  public void start() {
    WorkspacesErrorMailSenderProvider.setInstance(this);
  }

  @Override
  public void stop() {
  }

}
