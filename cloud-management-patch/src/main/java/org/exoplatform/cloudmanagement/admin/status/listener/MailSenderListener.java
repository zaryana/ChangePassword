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
package org.exoplatform.cloudmanagement.admin.status.listener;

import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_REGISTRATION_SUBJECT;
import static org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration.CLOUD_ADMIN_MAIL_REGISTRATION_TEMPLATE;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.WorkspacesMailSender;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.status.TenantStateListener;
import org.exoplatform.cloudmanagement.status.TenantStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Send mails with tenant registration information
 */
public class MailSenderListener implements TenantStateListener
{

   private static final Logger LOG = LoggerFactory.getLogger(MailSenderListener.class);

   private final CloudAdminConfiguration adminConfiguration;

   /**
    * @param cloudAdminConfiguration
    */
   public MailSenderListener(CloudAdminConfiguration cloudAdminConfiguration)
   {
      super();
      this.adminConfiguration = cloudAdminConfiguration;
   }

   /**
    * @see org.exoplatform.cloudmanagement.admin.status.TenantStateListener#onEvent(org.exoplatform.cloudmanagement.status.TenantStatus)
    */
   @Override
   public void onEvent(TenantStatus tenantStatus)
   {
      return; //does nothing
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Send mail with tenant registration information";
   }
}
