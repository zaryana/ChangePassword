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
package com.exoplatform.cloud.admin.mail;

import com.exoplatform.cloud.admin.dao.TenantInfoDataManager;
import com.exoplatform.cloud.admin.tenant.TenantStateDataManager;
import com.exoplatform.cloud.admin.util.MailSender;

import org.apache.commons.configuration.Configuration;

import java.util.Map;

public class WorkspacesTenantOperationMailSenderInitiator extends TenantOperationMailSenderInitiator
{
   public WorkspacesTenantOperationMailSenderInitiator(Configuration adminConfiguration,
      TenantStateDataManager tenantStateDataManager, TenantInfoDataManager tenantInfoDataManager, MailSender mailSender)
   {
      super(adminConfiguration, tenantStateDataManager, tenantInfoDataManager, mailSender);
   }

   @Override
   public void onTenantCreated(String tenantName, Map<String, String> agentParam)
   {
      // don't send email
   }

}
