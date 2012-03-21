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

import static java.net.HttpURLConnection.HTTP_OK;
import static org.exoplatform.cloudmanagement.admin.configuration.AdminConfiguration.CLOUD_ADMIN_FRONT_END_SERVER_HOST;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * TODO use TenantResumer
 */
public class TenantResumeThread implements Runnable
{

   private static final Logger LOG = LoggerFactory.getLogger(TenantResumeThread.class);

   private Configuration cloudAdminConfiguration;

   private String tName;

   public TenantResumeThread(Configuration cloudAdminConfiguration, String tName)
   {
      this.cloudAdminConfiguration = cloudAdminConfiguration;
      this.tName = tName;
   }

   @Override
   public void run()
   {
      HttpURLConnection connection = null;
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(cloudAdminConfiguration.getString(CLOUD_ADMIN_FRONT_END_SERVER_HOST, "localhost"));
      strUrl.append("/rest/cloud-admin/cloudworkspaces/tenant-service/resume?tenant=" + tName);
      try
      {
         URL url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");

         InputStream io;
         //read Response
         if (connection.getResponseCode() == HTTP_OK)
         {
            return;
         }
         else
         {
            if (connection.getResponseCode() < 400)
            {
               io = connection.getInputStream();
            }
            else
            {
               io = connection.getErrorStream();
            }

            String err = readText(connection.getErrorStream());
            String msg =
               ("Unable to resume workspace " + tName + " - HTTP status" + connection.getResponseCode() + (err != null
                  ? ". Server error: \r\n" + err + "\r\n" : ""));
            LOG.error(msg);
         }
      }
      catch (MalformedURLException e)
      {
         LOG.error(e.getMessage(), e);
      }
      catch (IOException e)
      {
         LOG.error(e.getMessage(), e);
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }

   protected String readText(InputStream errStream) throws IOException
   {
      if (errStream != null)
      {
         InputStreamReader errReader = new InputStreamReader(errStream);
         try
         {
            int r = -1;
            StringBuilder errText = new StringBuilder();
            char[] buff = new char[256];
            while ((r = errReader.read(buff)) >= 0)
            {
               errText.append(buff, 0, r);
            }
            return errText.toString();
         }
         finally
         {
            errReader.close();
         }
      }
      else
      {
         return null;
      }
   }

}
