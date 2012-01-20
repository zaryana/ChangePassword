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
package com.exoplatform.cloudworkspaces.organization.listener;

import static java.net.HttpURLConnection.HTTP_OK;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLimitListener extends UserEventListener
{

   protected static final Logger LOG = LoggerFactory.getLogger(UserLimitListener.class);

   public void preSave(User user, boolean isNew) throws Exception
   {

      URL url;
      HttpURLConnection connection = null;

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      RepositoryService reposervice = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);

      if (isNew)
      {
         if (user.getUserName().equals("root"))
            return;
         
         String tName = reposervice.getCurrentRepository().getConfiguration().getName();
         String masterhost = System.getProperty("tenant.masterhost");
         StringBuilder strUrl = new StringBuilder();
         strUrl.append("http://");
         strUrl.append(masterhost);
         strUrl.append("/rest/cloud-admin/public-tenant-service/isuserallowed/");
         strUrl.append(tName);
         strUrl.append("/");
         strUrl.append(user.getUserName());

         url = new URL(strUrl.toString());
         connection = (HttpURLConnection)url.openConnection();
         connection.setRequestMethod("GET");
         if (connection.getResponseCode() != HTTP_OK)
         {
            String err = readText(connection.getErrorStream());
            LOG.error("Unable to add user to workspace " + tName + " - HTTP status:" + connection.getResponseCode()
               + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
            throw new RepositoryException("Unable to add user" + user.getUserName() + " to workspace " + tName + " - HTTP confirmation error.");
         }
         else
         {
            String resp_body = "";
            String inputLine;
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((inputLine = in.readLine()) != null)
               resp_body = resp_body.concat(inputLine);
            in.close();
            if (resp_body.equalsIgnoreCase("TRUE"))
            {
               return;
            }
            else
            {
               throw new RepositoryException("Unable to add user " + user.getUserName() + " to workspace " + tName + " - limit reached");
            }
         }
      }
   }

   private String readText(InputStream errStream) throws IOException
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