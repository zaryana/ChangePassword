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
package com.exoplatform.cloudworkspaces.rest;

import org.apache.commons.configuration.Configuration;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfiguration;
import org.exoplatform.cloudmanagement.admin.configuration.ApplicationServerConfigurationManager;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/cloud-admin/cloudworkspaces/info-service")
public class CloudWorkspacesInfoService {

  private final ApplicationServerConfigurationManager applicationServerConfigurationManager;

  public CloudWorkspacesInfoService(ApplicationServerConfigurationManager applicationServerConfigurationManager) {
    this.applicationServerConfigurationManager = applicationServerConfigurationManager;
  }

  @GET
  @Path("/server-logs")
  @RolesAllowed({ "cloud-admin", "cloud-manager" })
  public String serverLogs() throws IOException {
    StringBuilder result = new StringBuilder();
    for (Configuration server : applicationServerConfigurationManager.getAllLoadedConfiguration()
                                                                     .values()) {
      result.append(createATag(generateServerLogUrl(server),
                               server.getString(ApplicationServerConfiguration.ALIAS_PARAMETER)));
      result.append("<br>");
    }
    return result.toString();
  }

  private String createATag(String url, String desc) {
    StringBuilder html = new StringBuilder();
    html.append("<a target='blank' href='");
    html.append(url);
    html.append("'>");
    if (desc == null)
      html.append(url);
    else
      html.append(desc);
    html.append("</a>");
    return html.toString();
  }

  private String generateServerLogUrl(Configuration server) {
    StringBuilder url = new StringBuilder();
    url.append("https://");
    url.append(server.getString("public.host"));
    url.append(':');
    url.append(System.getProperty("application.server.logs.port"));
    url.append("/logs/");
    return url.toString();
  }
}
