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
package org.exoplatform.cloudmanagement.admin.util;

import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_TEMPLATE;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatus;
import org.exoplatform.cloudmanagement.admin.status.ApplicationServerStatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WorkspacesServerStatusMailer extends ServerStatusMailer
{
   private static final Logger LOG = LoggerFactory.getLogger(ServerStatusMailer.class);

   private final MailSender mailSender;

   private final Configuration adminConfiguration;

   private final ApplicationServerStatusManager applicationServerStatusManager;

   public WorkspacesServerStatusMailer(MailSender mailSender, Configuration adminConfiguration,
      ApplicationServerStatusManager applicationServerStatusManager)
   {
      super(mailSender, adminConfiguration);
      this.mailSender = mailSender;
      this.adminConfiguration = adminConfiguration;
      this.applicationServerStatusManager = applicationServerStatusManager;
   }

   @Override
   public void sendLetterAboutServerState(String applicationServerAlias, boolean isServerOnMaintenance)
   {
      String mailTemplate = adminConfiguration.getString(CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_TEMPLATE);
      if (mailTemplate == null)
      {
         LOG.error("Warning mail template configuration not found");
      }
      else
      {
         Map<String, String> props = new HashMap<String, String>();
         if (!isServerOnMaintenance)
         {
            ApplicationServerStatus status =
               applicationServerStatusManager.getApplicationServerStatus(applicationServerAlias);
            if (status.isStarted())
               props.put("problem.with", "Application server " + applicationServerAlias);
            else
               props.put("problem.with", "Application server " + applicationServerAlias + " (start server during "
                  + (System.currentTimeMillis() - status.getStarted()) / 1000 / 60 + " minutes)");
            props.put("what.happend", "does not respond.");
            props.put("what.should.we.do", "Crash suspected, check it.");
         }
         else
         {
            props.put("problem.with", "Application server " + applicationServerAlias);
            props.put("what.happend", "seems to be working.");
            props.put("what.should.we.do", "Do not forget to remove maintenance state.");
         }

         String mailSubject;
         try
         {
            mailSubject = adminConfiguration.getString(CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_SUBJECT);

            for (Entry<String, String> entry : props.entrySet())
            {
               mailSubject = mailSubject.replace("${" + entry.getKey() + "}", entry.getValue());
            }

            for (String email : adminConfiguration.getStringArray(CLOUD_ADMIN_MAIL_ADMIN_EMAIL))
            {
               mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props);
            }
         }
         catch (CloudAdminException e)
         {
            LOG.warn(e.getLocalizedMessage());
         }
      }
   }
}
