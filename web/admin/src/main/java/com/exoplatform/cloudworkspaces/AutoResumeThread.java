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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutoResumeThread implements Runnable
{
   private static final Logger LOG = LoggerFactory.getLogger(AutoResumeThread.class);
   
   @Override
   public void run()
   {
      try
      {
         Thread.sleep(20000);
         StringBuilder strUrl = new StringBuilder();
         strUrl.append("http://");
         strUrl.append(System.getProperty("tenant.masterhost"));
         strUrl.append("/rest/cloud-admin/public-tenant-service/autojoin");
         URL url = new URL(strUrl.toString());
         HttpURLConnection connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
         {
            return;
         }
         else 
         {
            LOG.error("Unable to autojoin user. HTTP response: " + connection.getResponseCode());
         }

      }
      catch (InterruptedException e)
      {
         LOG.error("Unable to autojoin user", e);
      }
      catch (MalformedURLException e)
      {
         LOG.error("Unable to autojoin user", e);
      }
      catch (ProtocolException e)
      {
         LOG.error("Unable to autojoin user", e);
      }
      catch (IOException e)
      {
         LOG.error("Unable to autojoin user", e);
      }
   }

}
