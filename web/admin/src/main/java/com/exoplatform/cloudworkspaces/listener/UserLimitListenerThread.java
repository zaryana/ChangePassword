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
package com.exoplatform.cloudworkspaces.listener;

import com.exoplatform.cloudworkspaces.RequestState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserLimitListenerThread implements Runnable
{

   private static final Logger LOG = LoggerFactory.getLogger(UserLimitListenerThread.class);

   private long modificationTimeOnStartup;

   @Override
   public void run()
   {
      try
      {
         String maxUsersConfigurationFileName = System.getProperty("cloud.admin.userlimit");
         if (maxUsersConfigurationFileName == null)
         {
            LOG.warn("Maxusers configuration file not found, limit listener shutdown");
            return;
         }
         modificationTimeOnStartup = new File(maxUsersConfigurationFileName).lastModified();

         while (true)
         {
            Thread.sleep(120000); //2 min
            Long modificationTime = new File(maxUsersConfigurationFileName).lastModified();
            if (modificationTime > modificationTimeOnStartup)
            {
               if (LOG.isDebugEnabled())
                 LOG.debug("Limit file changed, trying to join users waiting for limit.");
               modificationTimeOnStartup = modificationTime;
               StringBuilder strUrl = new StringBuilder();
               strUrl.append("http://");
               strUrl.append(System.getProperty("tenant.masterhost"));
               strUrl.append("/rest/cloud-admin/cloudworkspaces/public-tenant-service/autojoin/"
                  + RequestState.WAITING_LIMIT.toString());
               URL url = new URL(strUrl.toString());
               HttpURLConnection connection = (HttpURLConnection)url.openConnection();
               connection.setRequestMethod("GET");
               if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
               {
                  LOG.error("Unable to autojoin user. HTTP response: " + connection.getResponseCode());
               }
            }
         }
      }
      catch (Exception e)
      {
         LOG.error("Exception in limit listener", e);
      }
   }

}
