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

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLimitListener extends UserEventListener {

  protected static final Logger LOG = LoggerFactory.getLogger(UserLimitListener.class);

  public void preSave(User user, boolean isNew) throws Exception {

    URL url;
    HttpURLConnection connection = null;

    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService reposervice = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    OrganizationService organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

    if (isNew) {
      if (user.getUserName().equals("root")) {
        return;
      }

      ListAccess<User> list = organizationService.getUserHandler().findAllUsers();

      String tName = reposervice.getCurrentRepository().getConfiguration().getName();
      String masterhost = System.getProperty("tenant.masterhost");
      StringBuilder strUrl = new StringBuilder();
      strUrl.append("http://");
      strUrl.append(masterhost);
      strUrl.append("/rest/cloud-admin/cloudworkspaces/tenant-service/maxallowed/");
      strUrl.append(tName);

      // try three times if maxallowed service will fail with 5xx status
      int attemptsNumber = 3;
      int responseCode;
      do {
        url = new URL(strUrl.toString());
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        responseCode = connection.getResponseCode();

        if (responseCode >= HTTP_INTERNAL_ERROR) {
          attemptsNumber--;
          try {
            Thread.sleep(10000);
          } catch (Throwable e) {
            LOG.warn("Error of thread sleep: " + e);
          }
        } else {
          attemptsNumber = 0;
        }
      } while (responseCode >= HTTP_INTERNAL_ERROR && attemptsNumber > 0);

      if (responseCode != HTTP_OK) {
        String err = readText(connection.getErrorStream());
        LOG.error("Unable to add user to workspace " + tName + " - HTTP status:"
            + connection.getResponseCode()
            + (err != null ? ". Server error: \r\n" + err + "\r\n" : ""));
        throw new UserLimitException("Unable to add user " + user.getUserName() + " to workspace "
            + tName + " - HTTP confirmation error (" + responseCode + ")");
      }

      StringBuilder responseBody = new StringBuilder();
      String inputLine;
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      try {
        while ((inputLine = in.readLine()) != null) {
          responseBody.append(inputLine);
        }
      } finally {
        in.close();
      }
      
      int maxAllowed;
      try {
        maxAllowed = Integer.parseInt(responseBody.toString());
      } catch(NumberFormatException e) {
        throw new UserLimitException("Error in maxallowed service response:", e);
      }
      
      if (maxAllowed == -1 || maxAllowed > list.getSize() - 1) { // minus root
        return;
      } else {
        throw new UserLimitException("Unable to add user " + user.getUserName() + " to workspace "
            + tName + " - limit reached");
      }
    }
  }

  private String readText(InputStream errStream) throws IOException {
    if (errStream != null) {
      InputStreamReader errReader = new InputStreamReader(errStream);
      try {
        int r = -1;
        StringBuilder errText = new StringBuilder();
        char[] buff = new char[256];
        while ((r = errReader.read(buff)) >= 0) {
          errText.append(buff, 0, r);
        }
        return errText.toString();
      } finally {
        errReader.close();
      }
    } else {
      return null;
    }
  }
}
