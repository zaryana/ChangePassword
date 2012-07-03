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
package org.exoplatform.cloudmanagement.admin.mail;

import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_EMAIL;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.MailConfiguration.CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.TenantInfoFieldName;
import org.exoplatform.cloudmanagement.admin.dao.TenantInfoDataManager;
import org.exoplatform.cloudmanagement.admin.status.AbstractTenantStateListener;
import org.exoplatform.cloudmanagement.admin.tenant.TenantStateDataManager;
import org.exoplatform.cloudmanagement.admin.util.MailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Send email's after changes in tenant states
 */
public class WorkspacesTenantOperationMailSenderInitiator extends AbstractTenantStateListener
{
   private static final Logger LOG = LoggerFactory.getLogger(WorkspacesTenantOperationMailSenderInitiator.class);

   private final Configuration adminConfiguration;

   private final TenantInfoDataManager tenantInfoDataManager;

   /**
    * @param tenantInfoDataManager
    */
   public WorkspacesTenantOperationMailSenderInitiator(Configuration adminConfiguration,
                                             TenantStateDataManager tenantStateDataManager, TenantInfoDataManager tenantInfoDataManager)
   {
      super(tenantStateDataManager);
      this.adminConfiguration = adminConfiguration;
      this.tenantInfoDataManager = tenantInfoDataManager;
   }

   /**
    * @see org.exoplatform.cloudmanagement.admin.status.AbstractTenantStateListener#onTenantCreated(java.lang.String,
    *      Map)
    */
   @Override
   public void onTenantCreated(String tenantName, Map<String, String> agentParam)
   {
      return; // do nothing
   }

   /**
    * @see org.exoplatform.cloudmanagement.admin.status.AbstractTenantStateListener#onTenantCreationFail(java.lang.String)
    */
   @Override
   public void onTenantCreationFail(String tenantName)
   {
      LOG.info("Tenant {} creation fail", tenantName);
      // send email to admins about problems
      String mailTemplate = adminConfiguration.getString(CLOUD_ADMIN_MAIL_ADMIN_ERROR_TEMPLATE);
      if (mailTemplate == null)
      {
         LOG.error("\"Tenant creation failed\" mail template configuration not found. Please contact support.");
      }
      try
      {
         String mailSubject = adminConfiguration.getString(CLOUD_ADMIN_MAIL_ADMIN_ERROR_SUBJECT);

         Map<String, String> props = new HashMap<String, String>();
         props.put("message", "Tenant <b>" + tenantName + "</b> creation failed!");
         props.put("exception.message", tenantInfoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_EXCEPTION));
         String trace = tenantInfoDataManager.getValue(tenantName, TenantInfoFieldName.PROPERTY_STACK_TRACE);
         if (trace != null)
         {
            props.put("stack.trace", trace.replace("\n", "<br>"));
         }

         MailSender mailSender = new MailSender(adminConfiguration);
         for (String email : adminConfiguration.getStringArray(CLOUD_ADMIN_MAIL_ADMIN_EMAIL))
         {
            mailSender.sendMail(email.trim(), mailSubject, mailTemplate, props);
         }
      }

      catch (CloudAdminException e)
      {
         LOG.error(e.getLocalizedMessage(), e);
      }

   }

}
