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
package com.exoplatform.cloud.admin.util;

import com.exoplatform.cloud.admin.configuration.ApplicationRecoveryConfiguration;
import com.exoplatform.cloud.admin.configuration.MailConfiguration;
import com.exoplatform.cloud.admin.status.ApplicationServerStatus;
import com.exoplatform.cloud.admin.status.ApplicationServerStatusManager;
import com.exoplatform.cloud.admin.util.MailSender.MailHeaders;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class WorkspacesServerStatusMailer extends ServerStatusMailer
{
   private static final Logger LOG = LoggerFactory.getLogger(ServerStatusMailer.class);

   /*
    * This is stub: to sending emails about server which starts long time, needs to save flag "mail was sent"/"or not sent"
    * 1 - one mail was sent (50%)
    * 2 - two mails was sent (75%)
    */
   private static final WeakHashMap<ApplicationServerStatus, Integer> startingMails =
      new WeakHashMap<ApplicationServerStatus, Integer>();

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
      if (!isServerOnMaintenance)
      {
         ApplicationServerStatus status =
            applicationServerStatusManager.getApplicationServerStatus(applicationServerAlias);
         if (!status.isStarted())
         {
            long maxStarting =
               adminConfiguration
                  .getLong(ApplicationRecoveryConfiguration.CLOUD_ADMIN_APPLICATION_SERVER_STARTING_MAX_TIME);
            long currentStarting = System.currentTimeMillis() - status.getStarted();
            if (currentStarting > 0.5 * maxStarting && !startingMails.containsKey(status))
            {
               startingMails.put(status, 1);
               sendLetterAboutServerStarting("starts more than 50% of starting limit time.", applicationServerAlias);
            }
            else if (currentStarting > 0.75 * maxStarting && startingMails.containsKey(status)
               && startingMails.get(status) < 2)
            {
               startingMails.put(status, 2);
               sendLetterAboutServerStarting("starts more than 75% of starting limit time.", applicationServerAlias);
            }
            else
            {
               return;
            }
         }
      }
      super.sendLetterAboutServerState(applicationServerAlias, isServerOnMaintenance);
   }

   private void sendLetterAboutServerStarting(String whatHappendMsg, String applicationServerAlias)
   {
      String mailTemplate = adminConfiguration.getString(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_TEMPLATE);
      if (mailTemplate == null)
      {
         LOG.error("Warning mail template configuration not found. Please, check property {} in admin.properties.",
            MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_TEMPLATE);
         return;
      }

      Map<String, String> props = new HashMap<String, String>();
      props.put("problem.with", "Application server " + applicationServerAlias);
      props.put("what.happend", whatHappendMsg);
      props.put("what.should.we.do", "Crash suspected, check it.");

      String mailSubject = adminConfiguration.getString(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_MAINTENANCE_SUBJECT);

      for (Entry<String, String> entry : props.entrySet())
      {
         mailSubject = mailSubject.replace("${" + entry.getKey() + "}", entry.getValue());
      }

      StringBuilder recipients = new StringBuilder();
      for (String nextRecipient : adminConfiguration.getStringArray(MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL))
      {
         recipients.append(nextRecipient).append(",");
      }

      Map<MailHeaders, String> mailHeaders =
         ImmutableMap.<MailHeaders, String> of(MailHeaders.TO, recipients.toString(), MailHeaders.SUBJECT, mailSubject);

      mailSender.sendMailQuietly(mailHeaders, mailTemplate, props);
   }

}
