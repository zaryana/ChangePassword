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
package com.exoplatform.cloudworkspaces;

import org.exoplatform.cloudmanagement.admin.CloudAdminException;
import org.exoplatform.cloudmanagement.admin.configuration.CloudAdminConfiguration;
import org.exoplatform.cloudmanagement.admin.queue.TenantQueueException;
import org.exoplatform.cloudmanagement.admin.status.CloudInfoHolder;
import org.exoplatform.cloudmanagement.status.TenantState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TenantCreatedListenerThread implements Runnable
{

   private static final Logger LOG = LoggerFactory.getLogger(TenantCreatedListenerThread.class);

   private static final String CLOUD_ADMIN_CREATION_TIMEOUT = "cloud.admin.tenant.creation.timeout";

   private String tName;

   private String email;

   private String firstName;

   private String companyName;

   private String lastName;

   private String phone;

   private String password;

   private CloudInfoHolder cloudInfoHolder;

   private int interval = 15000;

   private CloudAdminConfiguration cloudAdminConfiguration;
   

   public TenantCreatedListenerThread(String tName, String userMail, String firstName, String lastName, String companyName,
      String phone, String password, CloudInfoHolder cloudInfoHolder, CloudAdminConfiguration cloudAdminConfiguration)
   {
      this.tName = tName;
      this.email = userMail;
      this.firstName = firstName;
      this.lastName = lastName;
      this.companyName = companyName;
      this.phone = phone;
      this.password = password;
      this.cloudInfoHolder = cloudInfoHolder;
      this.cloudAdminConfiguration = cloudAdminConfiguration;

   }

   @Override
   public void run()
   {
      CloudIntranetUtils utils = new CloudIntranetUtils(cloudAdminConfiguration);
      int limit = Integer.parseInt(cloudAdminConfiguration.getProperty(CLOUD_ADMIN_CREATION_TIMEOUT, "86400"));

      if (cloudInfoHolder.isTenantExists(tName))
      {
         int count = 0;
         try
         {
            while (!cloudInfoHolder.getTenantStatus(tName).getState().equals(TenantState.ONLINE))
            {
               if (count > limit)
                  throw new CloudAdminException("Tenant creation timeout reached");
               Thread.sleep(interval);
               count+=15;
            }
            Thread.sleep(interval*4); //To let the proxy to reload;
            String root_password = UUID.randomUUID().toString().replace("-", "").substring(0, 9);
            String username = email.substring(0, (email.indexOf("@")));
            utils.storeUser(tName, email, firstName, lastName, password);
            utils.storeRoot(tName, email, "root", "root", root_password);
            Map<String, String> props = new HashMap<String, String>();
            props.put("tenant.masterhost", cloudAdminConfiguration.getMasterHost());
            props.put("tenant.repository.name", tName);
            props.put("user.mail", email);
            props.put("root.password", root_password);
            props.put("user.name", username);
            utils.sendIntranetCreatedEmails(email, props);
         }
         catch (TenantQueueException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
         catch (InterruptedException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
         catch (CloudAdminException e)
         {
            LOG.error(e.getMessage());
            utils.sendAdminErrorEmail("Unable to finish tenant '" + tName + "' creation", e);
         }
      }
      else
      {
         LOG.error("Unable to find tenant '" + tName + "' in creation queue.");
      }

   }

}
